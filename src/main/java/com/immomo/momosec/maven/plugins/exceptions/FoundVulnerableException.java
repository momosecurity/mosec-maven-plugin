package com.immomo.momosec.maven.plugins.exceptions;

import org.apache.maven.plugin.MojoFailureException;

public class FoundVulnerableException extends MojoFailureException {

    public FoundVulnerableException(String message) {
        super(message);
    }
}
