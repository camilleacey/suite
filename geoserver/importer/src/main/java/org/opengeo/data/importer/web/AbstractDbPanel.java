/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.opengeo.data.importer.web;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.geoserver.web.wicket.ParamResourceModel;
import org.geotools.data.DataStoreFactorySpi;
import org.opengeo.data.importer.Database;
import org.opengeo.data.importer.ImportData;

/**
 * Base class for database configuration panels.
 * 
 * @author Andrea Aime - OpenGeo
 * @author Justin Deoliveira, OpenGeo
 * 
 */
@SuppressWarnings("serial")
public abstract class AbstractDbPanel extends ImportSourcePanel {

    /**
     * available connection types
     */
    protected static final String CONNECTION_DEFAULT = "Default";
    protected static final String CONNECTION_JNDI = "JNDI";

    /** connection type */
    protected String connectionType;

    protected WebMarkupContainer paramPanelContainer;
    protected RepeatingView paramPanels;
    protected AdvancedDbParamPanel advancedParamPanel;

    protected LinkedHashMap<String, Component> paramPanelMap;

    public AbstractDbPanel(String id) {
        super(id);
        
        Form form = new Form("form");
        add(form);

        // connection type chooser
        paramPanelMap = buildParamPanels();
        connectionType = paramPanelMap.keySet().iterator().next();
        updatePanelVisibility(null);
        form.add(connectionTypeChoice(paramPanelMap));

        // default param panels
        paramPanelContainer = new WebMarkupContainer("paramPanelContainer");
        form.add(paramPanelContainer);
        paramPanelContainer.setOutputMarkupId(true);
        paramPanels = new RepeatingView("paramPanels");
        for (Component panel : paramPanelMap.values()) {
            paramPanels.add(panel);
        }
        paramPanelContainer.add(paramPanels);

        // advanced panel
        form.add(advancedParamPanel = buildAdvancedPanel("advancedPanel"));
    }

    public ImportData createImportSource() {
//        try {

          // build up the store connection param map
          Map<String, Serializable> params = new HashMap<String, Serializable>();
          DataStoreFactorySpi factory = fillStoreParams(params);

          return new Database(params);

//          // ok, check we can connect
//          DataAccess store = null;
//          try {
//              store = DataAccessFinder.getDataStore(params);
//              // force the store to open a connection
//              store.getNames();
//              store.dispose();
//          } catch (Throwable e) {
//              LOGGER.log(Level.INFO, "Could not connect to the datastore", e);
//              error(new ParamResourceModel("ImporterError.databaseConnectionError",
//                      AbstractDBMSPage.this, e.getMessage()).getString());
//              return;
//          } finally {
//              if (store != null)
//                  store.dispose();
//          }
//
//          // build the store
//          CatalogBuilder builder = new CatalogBuilder(getCatalog());
//          builder.setWorkspace(workspace);
//          StoreInfo si = builder.buildDataStore(generalParams.name);
//          si.setDescription(generalParams.description);
//          si.getConnectionParameters().putAll(params);
//          si.setEnabled(true);
//          si.setType(factory.getDisplayName());
//          getCatalog().add(si);
//
//          // redirect to the layer chooser
//          PageParameters pp = new PageParameters();
//          pp.put("store", si.getName());
//          pp.put("workspace", workspace.getName());
//          pp.put("storeNew", true);
//          pp.put("workspaceNew", false);
//          pp.put("skipGeometryless", isGeometrylessExcluded());
//          setResponsePage(VectorLayerChooserPage.class, pp);
//      } catch (Exception e) {
//          LOGGER.log(Level.SEVERE, "Error while setting up mass import", e);
//      }
//        return new DataStoreSource()
    };
    
    /**
     * Switches between the types of param panels
     */
    Component connectionTypeChoice(final Map<String, Component> paramPanelMap) {
        ArrayList<String> connectionTypeList = new ArrayList<String>(paramPanelMap.keySet());
        DropDownChoice choice = new DropDownChoice("connType", new PropertyModel(this,
                "connectionType"), new Model(connectionTypeList), new IChoiceRenderer() {

            public String getIdValue(Object object, int index) {
                return String.valueOf(object);
            }

            public Object getDisplayValue(Object object) {
                return new ParamResourceModel("ConnectionType." + object, null).getString();
            }
        });

        choice.add(new AjaxFormComponentUpdatingBehavior("onchange") {

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                updatePanelVisibility(target);
                target.addComponent(paramPanelContainer);
            }

        });

        return choice;
    }
    
    /**
     * Updates the panel visibility to show only the currently selected one.
     * Can also be used to perform actions when the panel visibility is updated
     *
     * @param paramPanelMap
     * @param target Used when doing ajax updates, might be null
     */
    protected void updatePanelVisibility(AjaxRequestTarget target) {
        for (String type : paramPanelMap.keySet()) {
            Component panel = paramPanelMap.get(type);
            panel.setVisible(connectionType.equals(type));
        }
    }

    /**
     * Setups the datastore and moves to the next page
     * 
     * @return
     */
//    SubmitLink submitLink() {
//        // TODO: fill this up with the required parameters
//        return new SubmitLink("next") {
//
//            @Override
//            public void onSubmit() {
//                try {
//                    // check there is not another store with the same name
//                    WorkspaceInfo workspace = generalParams.getWorkpace();
//                    NamespaceInfo namespace = getCatalog()
//                            .getNamespaceByPrefix(workspace.getName());
//                    StoreInfo oldStore = getCatalog().getStoreByName(workspace, generalParams.name,
//                            StoreInfo.class);
//                    if (oldStore != null) {
//                        error(new ParamResourceModel("ImporterError.duplicateStore",
//                                AbstractDBMSPage.this, generalParams.name, workspace.getName())
//                                .getString());
//                        return;
//                    }
//
//                    // build up the store connection param map
//                    Map<String, Serializable> params = new HashMap<String, Serializable>();
//                    DataStoreFactorySpi factory = fillStoreParams(namespace, params);
//
//                    // ok, check we can connect
//                    DataAccess store = null;
//                    try {
//                        store = DataAccessFinder.getDataStore(params);
//                        // force the store to open a connection
//                        store.getNames();
//                        store.dispose();
//                    } catch (Throwable e) {
//                        LOGGER.log(Level.INFO, "Could not connect to the datastore", e);
//                        error(new ParamResourceModel("ImporterError.databaseConnectionError",
//                                AbstractDBMSPage.this, e.getMessage()).getString());
//                        return;
//                    } finally {
//                        if (store != null)
//                            store.dispose();
//                    }
//
//                    // build the store
//                    CatalogBuilder builder = new CatalogBuilder(getCatalog());
//                    builder.setWorkspace(workspace);
//                    StoreInfo si = builder.buildDataStore(generalParams.name);
//                    si.setDescription(generalParams.description);
//                    si.getConnectionParameters().putAll(params);
//                    si.setEnabled(true);
//                    si.setType(factory.getDisplayName());
//                    getCatalog().add(si);
//
//                    // redirect to the layer chooser
//                    PageParameters pp = new PageParameters();
//                    pp.put("store", si.getName());
//                    pp.put("workspace", workspace.getName());
//                    pp.put("storeNew", true);
//                    pp.put("workspaceNew", false);
//                    pp.put("skipGeometryless", isGeometrylessExcluded());
//                    setResponsePage(VectorLayerChooserPage.class, pp);
//                } catch (Exception e) {
//                    LOGGER.log(Level.SEVERE, "Error while setting up mass import", e);
//                }
//
//            }
//        };
//    }
    
    /**
     * Builds and returns a map with parameter panels. 
     * <p>
     * The keys are used to fill in the drop down choice and to look for the i18n key using the 
     * "ConnectionType.${key}" convention. The panels built should have ids made of digits only, 
     * otherwise Wicket will complain about non safe ids in repeater.
     * </p>
     */
    protected abstract LinkedHashMap<String, Component> buildParamPanels();

    /**
     * Builds the advanced panel. 
     */
    protected AdvancedDbParamPanel buildAdvancedPanel(String id) {
        return new AdvancedDbParamPanel(id, false);
    }
    
    /**
     * Populates the connection parameters needed to connect to the datastore and returns the 
     * data store factory.
     * 
     * @param params Empty parameter map.
     */
    protected abstract DataStoreFactorySpi fillStoreParams(Map<String, Serializable> params);
}
