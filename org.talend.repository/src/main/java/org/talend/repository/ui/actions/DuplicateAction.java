// ============================================================================
//
// Copyright (C) 2006-2007 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.ui.actions;

import java.util.Arrays;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.talend.commons.exception.BusinessException;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.image.EImage;
import org.talend.commons.ui.image.ImageProvider;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.utils.KeywordsValidator;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.IProxyRepositoryFactory;
import org.talend.repository.model.IRepositoryService;
import org.talend.repository.model.ProxyRepositoryFactory;
import org.talend.repository.model.RepositoryConstants;
import org.talend.repository.model.RepositoryNode;
import org.talend.repository.model.RepositoryNodeUtilities;
import org.talend.repository.model.RepositoryNode.ENodeType;
import org.talend.repository.model.RepositoryNode.EProperties;
import org.talend.repository.model.actions.CopyObjectAction;

/**
 * DOC zwang class global comment. Detailled comment
 */
public class DuplicateAction extends AContextualAction {

    private RepositoryNode sourceNode = null;

    private IStructuredSelection selection = null;

    private static final String JOB_INIT_VERSION = "0.1";

    public DuplicateAction() {
        super();
        this.setText(Messages.getString("DuplicateAction.thisText.duplicate")); //$NON-NLS-1$
        this.setImageDescriptor(ImageProvider.getImageDesc(EImage.COPY_ICON));
    }

    public void init(TreeViewer viewer, IStructuredSelection selection) {

        boolean canWork = true;

        RepositoryNode node = (RepositoryNode) selection.getFirstElement();

        if (selection.isEmpty()) {
            setEnabled(false);
            return;
        }

        this.sourceNode = node;
        this.selection = selection;

        if (ProxyRepositoryFactory.getInstance().isUserReadOnlyOnCurrentProject()) {
            canWork = false;
        }

        if (selection != null) {
            if (((StructuredSelection) selection).toArray().length > 1) {
                canWork = false;
            } else if (((StructuredSelection) selection).toArray().length == 1) {
                Object obj = ((StructuredSelection) selection).toList().get(0);
                if (canWork) {
                    RepositoryNode sourceNode = (RepositoryNode) obj;
                    if (!CopyObjectAction.getInstance().validateAction(sourceNode, null)) {
                        canWork = false;
                    } else if (node.getProperties(EProperties.CONTENT_TYPE) == ERepositoryObjectType.JOB_DOC
                            || node.getProperties(EProperties.CONTENT_TYPE) == ERepositoryObjectType.JOBLET_DOC) {
                        canWork = false;
                    } else if (node.getContentType() == ERepositoryObjectType.JOBS
                            || node.getContentType() == ERepositoryObjectType.JOBLETS
                            || node.getContentType() == ERepositoryObjectType.GENERATED
                            || node.getProperties(EProperties.CONTENT_TYPE) == ERepositoryObjectType.JOB_DOC
                            || node.getProperties(EProperties.CONTENT_TYPE) == ERepositoryObjectType.JOBLET_DOC) {
                        canWork = false;
                    }
                }
            }
        } else {
            canWork = false;
        }

        setEnabled(canWork);
    }

    @Override
    public void run() {

        if (sourceNode == null) {
            return;
        }

        String initNameValue = "Copy_of_" + sourceNode.getObject().getProperty().getItem().getProperty().getLabel();

        CopyObjectAction copyObjectAction = CopyObjectAction.getInstance();

        final TreeSelection selectionInClipboard = (TreeSelection) selection;

        // see feature 0001563: Display "Save job" prompt when "copy" action for a job is requested.
        promptForSavingIfNecessary((RepositoryNode) selection.getFirstElement());

        String jobNameValue = null;

        try {
            jobNameValue = getDuplicateName(initNameValue, selectionInClipboard);
        } catch (BusinessException e) {
            jobNameValue = "";
        }

        InputDialog jobNewNameDialog = new InputDialog(null, Messages.getString("DuplicateAction.dialog.title"), Messages
                .getString("DuplicateAction.dialog.message"), jobNameValue, new IInputValidator() {

            public String isValid(String newText) {
                return validJobName(newText, selectionInClipboard);
            }

        });

        if (jobNewNameDialog.open() != Dialog.OK) {
            return;
        }

        String jobNewName = jobNewNameDialog.getValue();

        createOperation(jobNewName, sourceNode, copyObjectAction, selectionInClipboard);

        refresh();

    }

    public String getDuplicateName(String value, final TreeSelection selectionInClipboard) throws BusinessException {

        if (validJobName(value, selectionInClipboard) == null) {
            return value;
        } else {
            char j = 'a';
            String temp = value;
            while (validJobName(temp, selectionInClipboard) != null) {
                if (j > 'z') {
                    throw new BusinessException("Cannot generate pasted item label.");
                }
                temp = value + "_" + (j++) + "";
            }
            return temp;
        }
    }

    /**
     * 
     * DOC YeXiaowei Comment method "isValid".
     * 
     * @param itemName
     * @param selectionInClipboard
     * @return null means valid, other means some error exist
     */
    private String validJobName(String itemName, TreeSelection selectionInClipboard) {

        IRepositoryService service = (IRepositoryService) GlobalServiceRegister.getDefault().getService(IRepositoryService.class);
        IProxyRepositoryFactory repositoryFactory = service.getProxyRepositoryFactory();
        if (itemName.length() == 0) {
            return org.talend.core.i18n.Messages.getString("PropertiesWizardPage.NameEmptyError");
        } else if (!Pattern.matches(RepositoryConstants.getPattern(((RepositoryNode) selectionInClipboard.toArray()[0])
                .getObject().getType()), itemName)) {
            /*
             * maybe Messages.getString("PropertiesWizardPage.KeywordsError")
             */
            return org.talend.core.i18n.Messages.getString("PropertiesWizardPage.NameFormatError");
        } else {
            try {
                if (!repositoryFactory.isNameAvailable(createNewItem(), itemName)) {
                    return org.talend.core.i18n.Messages.getString("PropertiesWizardPage.ItemExistsError");
                }
            } catch (PersistenceException e) {
                return org.talend.core.i18n.Messages.getString("PropertiesWizardPage.ItemExistsError");
            }
            // see bug 0004157: Using specific name for (main) tream
            if (isKeyword(itemName)) {
                return org.talend.core.i18n.Messages.getString("PropertiesWizardPage.KeywordsError");
            }
        }

        return null;
    }

    /**
     * DOC hcw Comment method "isKeyword".
     * 
     * @param itemName
     * @return
     */
    private boolean isKeyword(String itemName) {
        ERepositoryObjectType itemType = sourceNode.getObjectType();
        ERepositoryObjectType[] types = { ERepositoryObjectType.PROCESS, ERepositoryObjectType.ROUTINES,
                ERepositoryObjectType.JOBS, ERepositoryObjectType.JOBLET, ERepositoryObjectType.JOBLETS };
        if (Arrays.asList(types).contains(itemType)) {
            return KeywordsValidator.isKeyword(itemName);
        }
        return false;
    }

    private Item createNewItem() {

        ERepositoryObjectType repositoryType = sourceNode.getObjectType();

        Item item = null;

        if (repositoryType.equals(ERepositoryObjectType.BUSINESS_PROCESS)) {
            item = PropertiesFactory.eINSTANCE.createBusinessProcessItem();
        } else if (repositoryType.equals(ERepositoryObjectType.CONTEXT)) {
            item = PropertiesFactory.eINSTANCE.createContextItem();
        } else if (repositoryType.equals(ERepositoryObjectType.ROUTINES)) {
            item = PropertiesFactory.eINSTANCE.createRoutineItem();
        } else if (repositoryType.equals(ERepositoryObjectType.SQLPATTERNS)) {
            item = PropertiesFactory.eINSTANCE.createSQLPatternItem();
        } else if (repositoryType.equals(ERepositoryObjectType.DOCUMENTATION)) {
            item = PropertiesFactory.eINSTANCE.createDocumentationItem();
            // Condition below is metadata items
        } else if (repositoryType.equals(ERepositoryObjectType.METADATA_CONNECTIONS)) {
            item = PropertiesFactory.eINSTANCE.createDatabaseConnectionItem();
        } else if (repositoryType.equals(ERepositoryObjectType.METADATA_FILE_DELIMITED)) {
            item = PropertiesFactory.eINSTANCE.createDelimitedFileConnectionItem();
        } else if (repositoryType.equals(ERepositoryObjectType.METADATA_FILE_POSITIONAL)) {
            item = PropertiesFactory.eINSTANCE.createPositionalFileConnectionItem();
        } else if (repositoryType.equals(ERepositoryObjectType.METADATA_FILE_REGEXP)) {
            item = PropertiesFactory.eINSTANCE.createRegExFileConnectionItem();
        } else if (repositoryType.equals(ERepositoryObjectType.METADATA_FILE_XML)) {
            item = PropertiesFactory.eINSTANCE.createXmlFileConnectionItem();
        } else if (repositoryType.equals(ERepositoryObjectType.METADATA_FILE_EXCEL)) {
            item = PropertiesFactory.eINSTANCE.createExcelFileConnectionItem();
        } else if (repositoryType.equals(ERepositoryObjectType.METADATA_FILE_LDIF)) {
            item = PropertiesFactory.eINSTANCE.createLdifFileConnectionItem();
        } else if (repositoryType.equals(ERepositoryObjectType.METADATA_LDAP_SCHEMA)) {
            item = PropertiesFactory.eINSTANCE.createLDAPSchemaConnectionItem();
        } else if (repositoryType.equals(ERepositoryObjectType.METADATA_SALESFORCE_SCHEMA)) {
            item = PropertiesFactory.eINSTANCE.createSalesforceSchemaConnectionItem();
        } else if (repositoryType.equals(ERepositoryObjectType.METADATA_GENERIC_SCHEMA)) {
            item = PropertiesFactory.eINSTANCE.createGenericSchemaConnectionItem();
        } else if (repositoryType.equals(ERepositoryObjectType.METADATA_WSDL_SCHEMA)) {
            item = PropertiesFactory.eINSTANCE.createWSDLSchemaConnectionItem();
        } else {
            item = PropertiesFactory.eINSTANCE.createProcessItem();
        }

        Property property = PropertiesFactory.eINSTANCE.createProperty();
        item.setProperty(property);
        return item;
    }

    private void createOperation(String newJobName, RepositoryNode target, CopyObjectAction copyObjectAction,
            TreeSelection selectionInClipboard) {

        Object currentSource = selectionInClipboard.toArray()[0];
        try {
            IPath path = RepositoryNodeUtilities.getPath(target);
            IProxyRepositoryFactory factory = ProxyRepositoryFactory.getInstance();

            if (((RepositoryNode) currentSource).getType().equals(ENodeType.REPOSITORY_ELEMENT)) {
                Item originalItem = ((RepositoryNode) currentSource).getObject().getProperty().getItem();
                String version = originalItem.getProperty().getVersion();
                String name = originalItem.getProperty().getLabel();
                // if (resetProcessVersion()) {
                originalItem.getProperty().setVersion(JOB_INIT_VERSION);
                // }
                originalItem.getProperty().setLabel(newJobName);
                factory.copy(originalItem, path, false);
                // restore originalItem
                originalItem.getProperty().setLabel(name);
                originalItem.getProperty().setVersion(version);
            }
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
    }

    /**
     * 
     * DOC YeXiaowei Comment method "resetProcessVersion".
     * 
     * @return
     */
    protected boolean resetProcessVersion() {
        return false;
    }
}
