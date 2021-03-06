package com.sap.olingo.jpa.metadata.core.edm.mapper.annotation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.util.Map;

import org.apache.olingo.commons.api.edm.provider.CsdlSchema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sap.olingo.jpa.metadata.core.edm.mapper.exception.ODataJPAModelException;

public class TestSchemaReader {
  private SchemaReader cut;

  @BeforeEach
  public void setup() {
    cut = new SchemaReader();
  }

  @Test
  public void TestGetNamespaceFromPath() throws IOException, ODataJPAModelException {
    Map<String, ? extends CsdlSchema> act;
    act = cut.getSchemas("annotations/Org.OData.Core.V1.xml");
    assertNotNull(act.get("Org.OData.Core.V1"));
    CsdlSchema schema = act.get("Org.OData.Core.V1");
    assertEquals("Org.OData.Core.V1", schema.getNamespace());
  }

  @Test
  public void TestGetAliasFromPath() throws IOException, ODataJPAModelException {
    Map<String, ? extends CsdlSchema> act;
    act = cut.getSchemas("annotations/Org.OData.Core.V1.xml");
    assertNotNull(act.get("Org.OData.Core.V1"));
    CsdlSchema schema = act.get("Org.OData.Core.V1");
    assertEquals("Core", schema.getAlias());
  }

  @Test
  public void TestGetTermsFromPath() throws IOException, ODataJPAModelException {
    Map<String, ? extends CsdlSchema> act;
    act = cut.getSchemas("annotations/Org.OData.Core.V1.xml");
    assertNotNull(act.get("Org.OData.Core.V1"));
    CsdlSchema schema = act.get("Org.OData.Core.V1");
    assertEquals(15, schema.getTerms().size());
  }

  @Test
  public void TestGetTypeDefinitionFromPath() throws IOException, ODataJPAModelException {
    Map<String, ? extends CsdlSchema> act;
    act = cut.getSchemas("annotations/Org.OData.Core.V1.xml");
    assertNotNull(act.get("Org.OData.Core.V1"));
    CsdlSchema schema = act.get("Org.OData.Core.V1");
    assertEquals(1, schema.getTypeDefinitions().size());
    assertNotNull(schema.getTypeDefinition("Tag"));
    assertEquals("Edm.Boolean", schema.getTypeDefinition("Tag").getUnderlyingType());
  }

  @Test
  public void TestGetEnumSchemaFromPath() throws IOException, ODataJPAModelException {
    Map<String, ? extends CsdlSchema> act;
    act = cut.getSchemas("annotations/Org.OData.Core.V1.xml");
    assertNotNull(act.get("Org.OData.Core.V1"));
    CsdlSchema schema = act.get("Org.OData.Core.V1");
    assertEquals(1, schema.getEnumTypes().size());
    assertNotNull(schema.getEnumType("Permission"));
    assertEquals(3, schema.getEnumType("Permission").getMembers().size());
    assertEquals("3", schema.getEnumType("Permission").getMember("ReadWrite").getValue());
  }

  @Test
  public void TestThrowsExceptionOnUnknownPath() throws IOException, ODataJPAModelException {
    assertThrows(ODataJPAModelException.class, () -> {
      cut.getSchemas("annotations/Org.OData.Core.V2.xml");
    });
  }

  @Test
  public void TestThrowsExceptionOnEmptyXML() throws IOException, ODataJPAModelException {

    assertThrows(IOException.class, () -> {
      cut.getSchemas("annotations/empty.xml");
    });
  }

//  csdlSchema.setEnumTypes(asEnumTypes());
//  csdlSchema.setComplexTypes(asComplexTypes());
//  csdlSchema.setTypeDefinitions(asTypeDefinitions());
}
