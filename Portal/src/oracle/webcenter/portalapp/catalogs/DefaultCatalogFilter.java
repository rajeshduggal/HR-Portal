package oracle.webcenter.portalapp.catalogs;

import java.util.Hashtable;

import java.util.logging.Level;

import oracle.adf.rc.catalog.CatalogElement;
import oracle.adf.rc.catalog.ElementAttributes;
import oracle.adf.rc.catalog.ElementAttribute;
import oracle.adf.rc.spi.plugin.catalog.CatalogDefinitionFilter;

import oracle.adf.share.logging.ADFLogger;

import oracle.webcenter.command.CommandException;
import oracle.webcenter.command.CommandService;
import oracle.webcenter.command.ScopeAPI;
import oracle.webcenter.framework.service.ServiceContext;

/**
 * Implementation of the CatalogDefinitionFilter interface that excludes 
 * catalog entries relating to WebCenter services that have not been provisioned
 * in the current application.
 *
 * This implementation looks for a resource catalog attribute named 
 * "WEBCENTER_SERVICE_ID" and uses this to determine if the specified service
 * is provisioned. If the service is provisioned, the entry is included. If it
 * is not provisioned, the entry is excluded. 
 *
 * Catalog entries that do not specified a WEBCENTER_SERVICE_ID are not 
 * affected by this filter.
 */
public class DefaultCatalogFilter implements CatalogDefinitionFilter
{
  private static final String CLASS_NAME = DefaultCatalogFilter.class.getName();
  private static final String WEBCENTER_SERVICE_ID = "WEBCENTER_SERVICE_ID";

  /**
   * @inheritDoc
   */
  public boolean includeInCatalog(CatalogElement catalogElement,
                                  Hashtable hashtable)
  {
    final String METHOD_NAME = "includeInCatalog";
    boolean includeElement = true;
    try
    {
      // Get the WebCenter Service ID of the element
      final ElementAttributes attributes = catalogElement.getLocalAttributes();
      if (attributes != null)
      {
        final ElementAttribute attribute = attributes.get(WEBCENTER_SERVICE_ID);
        String catalogServiceId = null;
        if (attribute != null)
        {
          catalogServiceId = attribute.getValue();
          includeElement = isServiceProvisioned(catalogServiceId);
        }
      }
    }
    catch (Exception e)
    {
      getLogger().logp(Level.WARNING, CLASS_NAME, METHOD_NAME, e.getMessage(), e);
      includeElement = false;
    }
    
    return includeElement; 
  }

  /**
   * Method to check if a given service is provisioned or not. 
   *
   * If the service does not implement Spaces Command API, then the service is 
   * considered not provisioned.
   *
   * @param currServiceId WebCenter Service ID
   * @return boolean.
   */
  private boolean isServiceProvisioned(String currServiceId)
  {
    final String METHOD_NAME = "isServiceProvisioned";
    final ADFLogger logger = getLogger();
    try
    {
      ScopeAPI cmd = CommandService.getScopeAPI(currServiceId);

      if (cmd == null)
      {
        if (logger.isWarning())
        {
          final StringBuilder builder = new StringBuilder(100);
          builder.append("Service ");
          builder.append(currServiceId);
          builder.append(" has not implemented the command API.");
          logger.warning(CLASS_NAME, METHOD_NAME,
                                    "Service " + currServiceId +
                                    " has not implemented the " +
                                    "command APIs");
        }
        return false;
      }

      // Get the scope for which the service is to be checked.
      String currSpaceName = ServiceContext.getContext().getScope().getName();

      if (cmd.isProvisioned(currSpaceName))
      {
        if (logger.isFine())
        {
          final StringBuilder builder = new StringBuilder(100);
          builder.append("Service ");
          builder.append(currServiceId);
          builder.append(" has been provisioned and catalog entries will be available.");
          logger.fine(CLASS_NAME, METHOD_NAME, builder.toString());
        }
        return true;
      }
      else
      {
        if (logger.isFine())
        {
          final StringBuilder builder = new StringBuilder(100);
          builder.append("Service ");
          builder.append(currServiceId);
          builder.append(" has been provisioned and catalog entries will be available.");
          logger.fine(CLASS_NAME, METHOD_NAME, builder.toString());
        }
        return true;
      }
    }
    catch (CommandException ex)
    {
      final StringBuilder builder = new StringBuilder(100);
      builder.append("Cannot determine if service ");
      builder.append(currServiceId);
      builder.append(" has been provisioned. ");
      getLogger().warning(CLASS_NAME, METHOD_NAME, builder.toString() , ex);
    }
    return false;
  }

  private ADFLogger getLogger()
  {
    return ADFLogger.createADFLogger(CLASS_NAME);
  }
}
