<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:mvn="http://maven.apache.org/POM/4.0.0">
<xsl:output method="text" omit-xml-declaration="yes" />
<xsl:template match="mvn:project">
<xsl:value-of select="mvn:version"/>
</xsl:template>

</xsl:stylesheet>
