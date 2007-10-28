package org.springframework.xml.schema.xsd;

import javax.xml.transform.Source;

/**
 * Represents an abstraction for an individual XSD schema document. A schema is made up of one or more schema
 * documents.
 *
 * @author Arjen Poutsma
 * @see XsdSchema
 * @since 1.0.2
 */
public interface XsdSchemaDocument {

    /** Return a filename for this schema document. For example, <code>schema.xsd</code>. */
    String getFilename();

    /** Returns the target namespace of the schema document. */
    String getTargetNamespace();

    /** Returns the {@link Source} of the schema document. */
    Source getSource();

    /** Returns the {@link XsdElement} objects of the schema document. */
    XsdElement[] getElements();

}
