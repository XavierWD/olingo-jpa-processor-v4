package com.sap.olingo.jpa.processor.core.query;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.criteria.From;
import javax.persistence.criteria.Subquery;

import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceLambdaAny;
import org.apache.olingo.server.api.uri.queryoption.expression.Expression;

import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAAssociationPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAEntityType;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAOnConditionItem;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAPath;
import com.sap.olingo.jpa.metadata.core.edm.mapper.api.JPAServiceDocument;
import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;
import com.sap.olingo.jpa.processor.core.exception.ODataJPAQueryException;
import com.sap.olingo.jpa.processor.core.filter.JPAFilterElementComplier;
import com.sap.olingo.jpa.processor.core.filter.JPAOperationConverter;

/**
 * Create a sub query to filter on collection properties e.g.
 * CollectionDeeps?$select=ID&$filter=FirstLevel/SecondLevel/Address/any(s:s/TaskID eq 'DEV').
 * This is done as sub-query instead of a join to have more straightforward way to implement OR or AND conditions
 * 
 * @author Oliver Grande
 *
 */
public final class JPACollectionFilterQuery extends JPANavigationQuery {

  public JPACollectionFilterQuery(final OData odata, final JPAServiceDocument sd, final EntityManager em,
      final JPAAbstractQuery parent, final List<UriResource> uriResourceParts, final Expression expression,
      final From<?, ?> from) throws ODataApplicationException {

    super(odata, sd, parent.jpaEntity, em, parent, from, determineAssoziation(parent.jpaEntity, uriResourceParts));
    // Create a sub-query having the key of the parent as result type
    this.subQuery = parent.getQuery().subquery(this.jpaEntity.getKeyType());
    this.aggregationType = null;
    createRoots(this.association);
    this.filterComplier = new JPAFilterElementComplier(odata, sd, em, jpaEntity,
        new JPAOperationConverter(cb, getContext().getOperationConverter()), null, this, expression, association);
  }

  private static JPAAssociationPath determineAssoziation(final JPAEntityType jpaEntity,
      final List<UriResource> uriResourceParts) throws ODataJPAQueryException {
    final StringBuilder pathName = new StringBuilder();
    int i = 0;
    while (uriResourceParts.get(i) != null && !(uriResourceParts.get(i) instanceof UriResourceLambdaAny)) {
      pathName.append(uriResourceParts.get(i).toString());
      pathName.append(JPAPath.PATH_SEPERATOR);
      i++;
    }
    pathName.deleteCharAt(pathName.lastIndexOf(JPAPath.PATH_SEPERATOR));
    try {
      return jpaEntity.getCollectionAttribute(pathName.toString()).asAssociation();
    } catch (ODataJPAModelException e) {
      throw new ODataJPAQueryException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> Subquery<T> getSubQueryExists(Subquery<?> childQuery) throws ODataApplicationException {

    if (this.queryJoinTable != null)
      createSubQueryJoinTable();
    return (Subquery<T>) this.subQuery;
  }

  @Override
  public From<?, ?> getRoot() {
    assert queryRoot != null;
    return queryRoot;
  }

  @Override
  protected void createSubQueryJoinTable() throws ODataApplicationException {
    /*
     * SELECT * FROM "BusinessPartner" AS B
     * WHERE "Type" = '2'
     * AND EXISTS (SELECT "BusinessPartnerID" FROM "Comment" AS C
     * WHERE B."ID" = C."BusinessPartnerID"
     * AND C."Text" LIKE '%just%')
     */
    try {
      final List<JPAOnConditionItem> right = association.getJoinTable().getInversJoinColumns();
      createSelectClause(subQuery, queryRoot, right);
      javax.persistence.criteria.Expression<Boolean> whereCondition = createWhereByAssociation(queryRoot, from,
          jpaEntity);
      subQuery.where(applyAdditionalFilter(whereCondition));
    } catch (ODataJPAModelException e) {
      throw new ODataJPAQueryException(e, HttpStatusCode.INTERNAL_SERVER_ERROR);
    }
  }
}
