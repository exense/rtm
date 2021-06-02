package org.rtm.rest;

import org.rtm.commons.RtmContext;

import javax.inject.Inject;

public class AbstractServlet {

    @Inject
    protected RtmContext context;
}
