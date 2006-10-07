<?xml version="1.0" encoding="UTF-8"?>
<stylesheet version="1.0" xmlns="http://www.w3.org/1999/XSL/Transform"
            xmlns:prod="http://www.springframework.org/spring-ws/test/transformation">
    <output method="xml" encoding="UTF-8"/>
    <template match="/">
        <prod:product>
            <prod:effectiveDate>
                <value-of select="prod:product/@effDate"/>
            </prod:effectiveDate>
            <prod:number>
                <value-of select="prod:product/prod:number"/>
            </prod:number>
            <prod:size>
                <value-of select="prod:product/prod:size"/>
            </prod:size>
        </prod:product>
    </template>
</stylesheet>
