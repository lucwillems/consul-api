package com.ecwid.consul.v1.acl;

import com.ecwid.consul.ConsulException;
import com.ecwid.consul.json.GsonFactory;
import com.ecwid.consul.transport.HttpResponse;
import com.ecwid.consul.transport.TLSConfig;
import com.ecwid.consul.v1.ConsulRawClient;
import com.ecwid.consul.v1.OperationException;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.acl.model.Acl;
import com.ecwid.consul.v1.acl.model.NewAcl;
import com.ecwid.consul.v1.acl.model.UpdateAcl;
import com.google.gson.reflect.TypeToken;

import java.util.List;
import java.util.Map;

/**
 * @author Vasily Vasilkov (vgv@ecwid.com)
 */
public final class AclConsulClient implements AclClient {

	private final ConsulRawClient rawClient;

	public AclConsulClient(ConsulRawClient rawClient) {
		this.rawClient = rawClient;
	}

	public AclConsulClient() {
		this(new ConsulRawClient());
	}

	public AclConsulClient(TLSConfig tlsConfig) {
		this(new ConsulRawClient(tlsConfig));
	}

	public AclConsulClient(String agentHost) {
		this(new ConsulRawClient(agentHost));
	}

	public AclConsulClient(String agentHost, TLSConfig tlsConfig) {
		this(new ConsulRawClient(agentHost, tlsConfig));
	}

	public AclConsulClient(String agentHost, int agentPort) {
		this(new ConsulRawClient(agentHost, agentPort));
	}

	public AclConsulClient(String agentHost, int agentPort, TLSConfig tlsConfig) {
		this(new ConsulRawClient(agentHost, agentPort, tlsConfig));
	}

	@Override
	public Response<String> aclCreate(NewAcl newAcl, String token) {
		String json = GsonFactory.getGson().toJson(newAcl);
		HttpResponse httpResponse = rawClient.makePutRequest("/v1/acl/create", json,token);

		if (httpResponse.getStatusCode() == 200) {
			Map<String, String> value = GsonFactory.getGson().fromJson(httpResponse.getContent(), new TypeToken<Map<String, String>>() {
			}.getType());
			return new Response<String>(value.get("ID"), httpResponse);
		} else {
			throw new OperationException(httpResponse);
		}
	}

	@Override
	public Response<Void> aclUpdate(UpdateAcl updateAcl, String token) {
		String json = GsonFactory.getGson().toJson(updateAcl);
		HttpResponse httpResponse = rawClient.makePutRequest("/v1/acl/update", json, token);

		if (httpResponse.getStatusCode() == 200) {
			return new Response<Void>(null, httpResponse);
		} else {
			throw new OperationException(httpResponse);
		}
	}

	@Override
	public Response<Void> aclDestroy(String aclId, String token) {
		HttpResponse httpResponse = rawClient.makePutRequest("/v1/acl/destroy/" + aclId, "", token);

		if (httpResponse.getStatusCode() == 200) {
			return new Response<Void>(null, httpResponse);
		} else {
			throw new OperationException(httpResponse);
		}
	}

	@Override
	public Response<Acl> getAcl(String id) {
		HttpResponse httpResponse = rawClient.makeGetRequest("/v1/acl/info/" + id);

		if (httpResponse.getStatusCode() == 200) {
			List<Acl> value = GsonFactory.getGson().fromJson(httpResponse.getContent(), new TypeToken<List<Acl>>() {
			}.getType());

			if (value.isEmpty()) {
				return new Response<Acl>(null, httpResponse);
			} else if (value.size() == 1) {
				return new Response<Acl>(value.get(0), httpResponse);
			} else {
				throw new ConsulException("Strange response (list size=" + value.size() + ")");
			}
		} else {
			throw new OperationException(httpResponse);
		}
	}

	@Override
	public Response<String> aclClone(String aclId, String token) {
		HttpResponse httpResponse = rawClient.makePutRequest("/v1/acl/clone/" + aclId, "", token);

		if (httpResponse.getStatusCode() == 200) {
			Map<String, String> value = GsonFactory.getGson().fromJson(httpResponse.getContent(), new TypeToken<Map<String, String>>() {
			}.getType());
			return new Response<String>(value.get("ID"), httpResponse);
		} else {
			throw new OperationException(httpResponse);
		}
	}

	@Override
	public Response<List<Acl>> getAclList(String token) {
		HttpResponse httpResponse = rawClient.makeGetRequest("/v1/acl/list", token);

		if (httpResponse.getStatusCode() == 200) {
			List<Acl> value = GsonFactory.getGson().fromJson(httpResponse.getContent(), new TypeToken<List<Acl>>() {
			}.getType());
			return new Response<List<Acl>>(value, httpResponse);
		} else {
			throw new OperationException(httpResponse);
		}
	}

}
