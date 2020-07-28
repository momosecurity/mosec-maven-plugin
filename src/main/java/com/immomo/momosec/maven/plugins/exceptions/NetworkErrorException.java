package com.immomo.momosec.maven.plugins.exceptions;

import org.apache.maven.plugin.AbstractMojoExecutionException;

public class NetworkErrorException extends AbstractMojoExecutionException {

    public NetworkErrorException(String message) {
        super(message);
    }
}
