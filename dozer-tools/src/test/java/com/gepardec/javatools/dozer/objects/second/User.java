/*
 * This file was generated using mdsd-maven-plugin 1.2.0.
 * Any modifications to this file will be lost upon regeneration.
 *
 * Generated: 2015-03-04T15:14:55+0100
 */

package com.gepardec.javatools.dozer.objects.second;

import java.util.List;


public class User {

    private String id;
    private String name;
    private List<Role> roles;
	public List<Role> getRoles() {
		return roles;
	}
	public void setRoles(List<Role> roles) {
		this.roles = roles;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

}
