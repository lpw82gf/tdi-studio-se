// ============================================================================
//
// Copyright (C) 2006-2010 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.talend.core.database.EDatabaseTypeName;
import org.talend.core.model.metadata.IMetadataConnection;
import org.talend.core.model.metadata.builder.ConvertionHelper;
import org.talend.core.model.metadata.builder.connection.DatabaseConnection;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.metadata.builder.database.EDatabaseSchemaOrCatalogMapping;
import org.talend.core.model.metadata.builder.database.ExtractMetaDataUtils;
import org.talend.cwm.helper.CatalogHelper;
import org.talend.cwm.helper.ConnectionHelper;
import org.talend.cwm.helper.PackageHelper;
import org.talend.cwm.helper.SchemaHelper;
import orgomg.cwm.resource.relational.Catalog;
import orgomg.cwm.resource.relational.Schema;

/**
 * DOC hywang class global comment. Detailled comment
 */
public class ProjectNodeHelper {

    /*
     * To make the database connection only show and refresh the tables in the specified datapackage when change the
     * value of database connection's SID and UISchema
     */
    /* return all tables from current datapackage with set,so that the result is disorted */
    public static Set<org.talend.core.model.metadata.builder.connection.MetadataTable> getTablesFromSpecifiedDataPackage(
            DatabaseConnection dbconn) {
        String schema = dbconn.getUiSchema();
        String catalog = dbconn.getSID();
        String databaseType = dbconn.getDatabaseType();
        EDatabaseTypeName currentType = EDatabaseTypeName.getTypeFromDbType(databaseType);
        EDatabaseSchemaOrCatalogMapping curCatalog = currentType.getCatalogMappingField();
        EDatabaseSchemaOrCatalogMapping curSchema = currentType.getSchemaMappingField();
        if (curCatalog != null && curSchema != null) {
            switch (curCatalog) {
            case Login:
                catalog = dbconn.getUsername();
                break;
            case None:
                catalog = "";
                break;
            }
            switch (curSchema) {
            case Login:
                schema = dbconn.getUsername();
                break;
            case Schema:
                schema = dbconn.getUiSchema();
                break;
            case None:
                schema = "";
                break;
            case Default_Name:
                schema = dbconn.getName(); // label for default name for
                // access or such kind of
                // non-catalogs databases
                break;
            }
        }
        return getTablesFromCurrentCatalogOrSchema(catalog, schema, dbconn);
    }

    public static Set<org.talend.core.model.metadata.builder.connection.MetadataTable> getTablesFromCurrentCatalogOrSchema(
            String dbsid, String schema, DatabaseConnection dbconn) {

        Set<org.talend.core.model.metadata.builder.connection.MetadataTable> allTables = new HashSet<org.talend.core.model.metadata.builder.connection.MetadataTable>();
        /* context model show all tables */
        if (dbconn.isContextMode()) {
            allTables = ConnectionHelper.getTables(dbconn);
        } else {
            boolean hasSchemaInCatalog = false;
            Catalog c = (Catalog) ConnectionHelper.getPackage(dbsid, dbconn, Catalog.class);
            Schema s = (Schema) ConnectionHelper.getPackage(schema, dbconn, Schema.class);
            List<Schema> subschemas = new ArrayList<Schema>();
            if (c != null) {
                subschemas = CatalogHelper.getSchemas(c);
                hasSchemaInCatalog = subschemas.size() > 0;
            }
            if (c != null && s == null && !hasSchemaInCatalog) { // only catalog
                PackageHelper.getAllTables(c, allTables);
                // PackageHelper.addMetadataTable(dbtable, c);

            } else if (s != null && !hasSchemaInCatalog && c == null) { // only schema
                PackageHelper.getAllTables(s, allTables);
                // PackageHelper.addMetadataTable(dbtable, s);
            } else if (c != null && hasSchemaInCatalog) { // both schema and catalog
                subschemas = CatalogHelper.getSchemas(c);
                hasSchemaInCatalog = subschemas.size() > 0;
                if (subschemas.size() > 0) {
                    for (Schema current : subschemas) {
                        if (current.getName().equals(schema)) {
                            s = current;
                            break;
                        }
                    }
                    /**
                     * if dont specifc a schema because of getUiSchema() is null,show all cataogs table by default,or it
                     * will cause bug 0016578
                     */
                    if (s == null || "".equals(s)) {
                        // allTables = ConnectionHelper.getTables(dbconn);
                        PackageHelper.getAllTables(c, allTables);
                    } else {
                        PackageHelper.getAllTables(s, allTables);
                    }
                    // PackageHelper.addMetadataTable(dbtable, s);
                }
            } else {
                // return nothing
            }
        }
        return allTables;
    }

    /*
     * To make the database connection only show and refresh the tables in the specified datapackage when change the
     * value of database connection's SID and UISchema
     */
    /* return all tables from current datapackage with List,so that the result is order-sorted */
    public static List<org.talend.core.model.metadata.builder.connection.MetadataTable> getTablesFromSpecifiedDataPackageWithOders(
            DatabaseConnection dbconn) {
        // if the database connection is contextmodel, need to get the original value of every parameter
        String schema = dbconn.getUiSchema();
        String catalog = dbconn.getSID();
        String databaseType = dbconn.getDatabaseType();
        EDatabaseTypeName currentType = EDatabaseTypeName.getTypeFromDbType(databaseType);
        EDatabaseSchemaOrCatalogMapping curCatalog = currentType.getCatalogMappingField();
        EDatabaseSchemaOrCatalogMapping curSchema = currentType.getSchemaMappingField();
        if (curCatalog != null && curSchema != null) {
            switch (curCatalog) {
            case Login:
                catalog = dbconn.getUsername();
                break;
            case None:
                catalog = "";
                break;
            }
            switch (curSchema) {
            case Login:
                schema = dbconn.getUsername();
                break;
            case Schema:
                schema = dbconn.getUiSchema();
                break;
            case None:
                schema = "";
                break;
            case Default_Name:
                schema = dbconn.getName(); // label for default name for
                // access or such kind of
                // non-catalogs databases
                break;
            }
        }
        return getTablesFromCurrentCatalogOrSchemaWithOrders(catalog, schema, dbconn);
    }

    public static List<org.talend.core.model.metadata.builder.connection.MetadataTable> getTablesFromCurrentCatalogOrSchemaWithOrders(
            String dbsid, String schema, DatabaseConnection dbconn) {

        List<org.talend.core.model.metadata.builder.connection.MetadataTable> allTables = new ArrayList<org.talend.core.model.metadata.builder.connection.MetadataTable>();
        /* context model show all tables */
        if (dbconn.isContextMode()) {
            allTables = ConnectionHelper.getTablesWithOrders(dbconn);
        } else {
            boolean hasSchemaInCatalog = false;
            Catalog c = (Catalog) ConnectionHelper.getPackage(dbsid, dbconn, Catalog.class);
            Schema s = (Schema) ConnectionHelper.getPackage(schema, dbconn, Schema.class);
            List<Schema> subschemas = new ArrayList<Schema>();
            if (c != null) {
                subschemas = CatalogHelper.getSchemas(c);
                hasSchemaInCatalog = subschemas.size() > 0;
            }
            if (c != null && s == null && !hasSchemaInCatalog) { // only catalog
                PackageHelper.getAllTablesWithOrders(c, allTables);
                // PackageHelper.addMetadataTable(dbtable, c);

            } else if (s != null && !hasSchemaInCatalog && c == null) { // only schema
                PackageHelper.getAllTablesWithOrders(s, allTables);
                // PackageHelper.addMetadataTable(dbtable, s);
            } else if (c != null && hasSchemaInCatalog) { // both schema and catalog
                subschemas = CatalogHelper.getSchemas(c);
                hasSchemaInCatalog = subschemas.size() > 0;
                if (subschemas.size() > 0) {
                    for (Schema current : subschemas) {
                        if (current.getName().equals(schema)) {
                            s = current;
                            break;
                        }
                    }
                    /**
                     * if dont specifc a schema because of getUiSchema() is null,show all cataogs table by default,or it
                     * will cause bug 0016578
                     */
                    if (s == null || "".equals(s)) {
                        // allTables = ConnectionHelper.getTables(dbconn);
                        PackageHelper.getAllTablesWithOrders(c, allTables);
                    } else {
                        PackageHelper.getAllTablesWithOrders(s, allTables);
                    }
                    // PackageHelper.addMetadataTable(dbtable, s);
                }
            } else {
                // return nothing

            }
        }
        return allTables;
    }

    public static void addTableForSpecifiedDataPackage(DatabaseConnection dbconn, MetadataTable dbtable) {
        // if the database connection is contextmodel, need to get the original value of every parameter
        IMetadataConnection imetadataConnection = ConvertionHelper.convert(dbconn);
        String schema = imetadataConnection.getSchema();
        String catalog = imetadataConnection.getDatabase();
        String databaseType = imetadataConnection.getDbType();
        EDatabaseTypeName currentType = EDatabaseTypeName.getTypeFromDbType(databaseType);
        EDatabaseSchemaOrCatalogMapping curCatalog = currentType.getCatalogMappingField();
        EDatabaseSchemaOrCatalogMapping curSchema = currentType.getSchemaMappingField();
        if (curCatalog != null && curSchema != null) {
            switch (curCatalog) {
            case Login:
                catalog = imetadataConnection.getUsername();
                break;
            case None:
                catalog = "";
                break;
            }
            switch (curSchema) {
            case Login:
                schema = imetadataConnection.getUsername();
                break;
            case Schema:
                schema = imetadataConnection.getSchema();
                break;
            case None:
                schema = "";
                break;
            case Default_Name:
                schema = dbconn.getName(); // label for default name for
                // access or such kind of
                // non-catalogs databases
                break;
            }
        }
        addTableForTemCatalogOrSchema(catalog, schema, dbconn, dbtable, imetadataConnection);
    }

    public static void addTableForTemCatalogOrSchema(String dbsid, String schema, DatabaseConnection connection,
            MetadataTable dbtable, IMetadataConnection iMetadataConnection) {
        boolean hasSchemaInCatalog = false;
        Catalog c = (Catalog) ConnectionHelper.getPackage(dbsid, connection, Catalog.class);
        Schema s = (Schema) ConnectionHelper.getPackage(schema, connection, Schema.class);
        List<Schema> subschemas = new ArrayList<Schema>();
        if (c != null) {
            subschemas = CatalogHelper.getSchemas(c);
            hasSchemaInCatalog = subschemas.size() > 0;
        }
        if (c != null && s == null && !hasSchemaInCatalog) { // only catalog
            PackageHelper.addMetadataTable(dbtable, c);

        } else if (s != null && !hasSchemaInCatalog && c == null) { // only schema
            PackageHelper.addMetadataTable(dbtable, s);
        } else if (c != null && hasSchemaInCatalog) { // both schema and catalog
            subschemas = CatalogHelper.getSchemas(c);
            hasSchemaInCatalog = subschemas.size() > 0;
            if (subschemas.size() > 0) {
                for (Schema current : subschemas) {
                    if (current.getName().equals(schema)) {
                        s = current;
                        break;
                    }
                }
                PackageHelper.addMetadataTable(dbtable, s);
            }
        } else {
            /*
             * if there is no catalog or schema,create the structure correctly rather than always create a catalog,found
             * this issue when fixing bug 16636
             */
            ProjectNodeHelper.addCatalogOrSchema(iMetadataConnection, connection);
            addTableForTemCatalogOrSchema(dbsid, schema, connection, dbtable, iMetadataConnection);
        }
    }

    /* create catalog or schema for a database connection,the structure is the same as TOP */
    public static void addCatalogOrSchema(IMetadataConnection metadataConnection, DatabaseConnection dbconn) {
        EDatabaseSchemaOrCatalogMapping catalog = null;
        EDatabaseSchemaOrCatalogMapping schema = null;
        EDatabaseTypeName type = EDatabaseTypeName.getTypeFromDbType(metadataConnection.getDbType());
        if (type.equals(EDatabaseTypeName.GENERAL_JDBC)) {
            String realtype = ExtractMetaDataUtils.getDbTypeByClassName(metadataConnection.getDriverClass());
            type = EDatabaseTypeName.getTypeFromDbType(realtype);
            catalog = type.getCatalogMappingField();
            schema = type.getSchemaMappingField();
        } else {
            catalog = type.getCatalogMappingField();
            schema = type.getSchemaMappingField();
        }
        fillValuesForSchemaOrCatalog(catalog, schema, metadataConnection, dbconn);
    }

    private static void fillValuesForSchemaOrCatalog(EDatabaseSchemaOrCatalogMapping catalog,
            EDatabaseSchemaOrCatalogMapping schema, IMetadataConnection metadataConnection, DatabaseConnection dbconn) {
        Schema s = null;
        Catalog c = null;
        List<Schema> schemas = new ArrayList<Schema>();
        String user = metadataConnection.getUsername();
        String defaultname = dbconn.getName();
        String dbsid = metadataConnection.getDatabase();
        String dbuischema = metadataConnection.getSchema();
        if (schema != null && catalog != null) {
            if (schema.equals(EDatabaseSchemaOrCatalogMapping.None) && !catalog.equals(EDatabaseSchemaOrCatalogMapping.None)) {// only
                // catalog
                if (catalog.equals(EDatabaseSchemaOrCatalogMapping.Sid)) {
                    c = CatalogHelper.createCatalog(dbsid);
                    c.getDataManager().add(dbconn);
                    ConnectionHelper.addCatalog(c, dbconn);
                }

            } else if (!schema.equals(EDatabaseSchemaOrCatalogMapping.None) // only schema
                    && catalog.equals(EDatabaseSchemaOrCatalogMapping.None)) {
                if (schema.equals(EDatabaseSchemaOrCatalogMapping.Schema)) {
                    s = SchemaHelper.createSchema(dbuischema);
                    s.getDataManager().add(dbconn);
                    ConnectionHelper.addSchema(s, dbconn);
                }
                if (schema.equals(EDatabaseSchemaOrCatalogMapping.Login)) {
                    s = SchemaHelper.createSchema(user);
                    s.getDataManager().add(dbconn);
                    ConnectionHelper.addSchema(s, dbconn);
                }
                if (schema.equals(EDatabaseSchemaOrCatalogMapping.Default_Name)) { // for databases like access
                    s = SchemaHelper.createSchema(defaultname);
                    s.getDataManager().add(dbconn);
                    ConnectionHelper.addSchema(s, dbconn);
                }
            } else { // both schema and catalog
                String cvalue = dbsid;
                String svalue = null;
                cvalue = dbsid;
                switch (schema) {
                case Sid:
                    svalue = dbsid;
                    break;
                case Schema:
                    svalue = dbuischema;
                    break;
                case Login:
                    svalue = user;
                    break;
                }
                c = CatalogHelper.createCatalog(cvalue);
                s = SchemaHelper.createSchema(svalue);
                schemas.add(s);
                CatalogHelper.addSchemas(schemas, c);
                c.getDataManager().add(dbconn);
                ConnectionHelper.addCatalog(c, dbconn);
            }
        }
    }
}
