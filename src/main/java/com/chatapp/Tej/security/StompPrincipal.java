package com.chatapp.Tej.security;

import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.security.Principal;

@RequiredArgsConstructor
public class StompPrincipal implements Principal , Serializable {
    private final String name;

    @Override
    public String getName(){return name;}
}
