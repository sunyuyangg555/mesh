package com.gentics.mesh.core.data.service;

import io.vertx.ext.apex.RoutingContext;

import com.gentics.mesh.core.Page;
import com.gentics.mesh.core.data.model.root.GroupRoot;
import com.gentics.mesh.core.data.model.tinkerpop.Group;
import com.gentics.mesh.core.data.model.tinkerpop.User;
import com.gentics.mesh.core.rest.group.response.GroupResponse;
import com.gentics.mesh.paging.PagingInfo;

public interface GroupService {

	public Group findByUUID(String uuid);

	public Group findByName(String name);

	public GroupResponse transformToRest(RoutingContext rc, Group group);

	public Page<Group> findAllVisible(User requestUser, PagingInfo pagingInfo);

	public Group create(String name);

	public GroupRoot findRoot();

	public GroupRoot createRoot();

	public void delete(Group group);

}
