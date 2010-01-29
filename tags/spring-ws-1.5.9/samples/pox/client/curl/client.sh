#!/bin/sh
curl --header "Content-type: application/xml" -v --data @contactsRequest.xml http://localhost:8080/pox/
