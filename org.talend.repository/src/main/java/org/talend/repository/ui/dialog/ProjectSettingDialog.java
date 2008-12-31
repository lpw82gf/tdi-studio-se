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
package org.talend.repository.ui.dialog;

import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.ProjectSettingNode;

/**
 * DOC aimingchen class global comment. Detailled comment
 */
public class ProjectSettingDialog {

    private static final String TITLE = Messages.getString("ProjectSettingDialog.Title"); //$NON-NLS-1$

    public ProjectSettingDialog() {

    }

    /**
     * 
     * get all projectsettingPage node
     * 
     * @return PreferenceManager
     */
    private PreferenceManager getNodeManager() {
        PreferenceManager manager = new PreferenceManager();
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IConfigurationElement[] configurationElements = registry
                .getConfigurationElementsFor("org.talend.repository.projectsetting_page"); //$NON-NLS-1$
        for (int i = 0; i < configurationElements.length; i++) {
            IConfigurationElement element = configurationElements[i];
            ProjectSettingNode node = new ProjectSettingNode(element);
            try {
                IPreferencePage page = (IPreferencePage) element.createExecutableExtension("class");
                node.setPage(page);
                page.setDescription(element.getAttribute("description"));
                page.setTitle(element.getAttribute("title"));
            } catch (CoreException e) {
                ExceptionHandler.process(e);
            }
            String category = node.getCategory();
            if (category == null) {
                manager.addToRoot(node);
            } else {
                IPreferenceNode parent = manager.find(category);
                if (parent != null) {
                    parent.add(node);
                }
            }
        }
        IPreferenceNode[] rootSubNodes = manager.getRootSubNodes();

        // sort the rootSubNodes
        Arrays.sort(rootSubNodes, new Comparator() {

            public int compare(Object o1, Object o2) {
                if (o1 instanceof ProjectSettingNode && o2 instanceof ProjectSettingNode) {
                    ProjectSettingNode node1 = (ProjectSettingNode) o1;
                    ProjectSettingNode node2 = (ProjectSettingNode) o2;
                    if (node1.getOrder() != null && node2.getOrder() != null) {
                        return node1.getOrder().compareTo(node2.getOrder());
                    }
                }
                return -1;
            }
        });
        manager.removeAll();
        // add the sorted list to manager
        for (int i = 0; i < rootSubNodes.length; i++) {
            manager.addToRoot(rootSubNodes[i]);
        }
        return manager;
    }

    public void open() {
        PreferenceManager manager = getNodeManager();
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        final PreferenceDialog dialog = new PreferenceDialog(shell, manager);
        BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {

            public void run() {
                dialog.create();
                dialog.getShell().setText(TITLE);
                dialog.open();
            }
        });
    }
}
