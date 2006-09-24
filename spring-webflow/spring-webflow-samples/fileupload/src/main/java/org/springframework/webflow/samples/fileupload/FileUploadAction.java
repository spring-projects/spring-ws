package org.springframework.webflow.samples.fileupload;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.webflow.Event;
import org.springframework.webflow.RequestContext;
import org.springframework.webflow.action.AbstractAction;

public class FileUploadAction extends AbstractAction {
	protected Event doExecute(RequestContext context) throws Exception {
		MultipartFile file = context.getRequestParameters().getMultipartFile("file");
		context.getRequestScope().put("file", file.getBytes());
		return success();
	}
}
