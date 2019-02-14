package com.neelnetwork.api.http.alias

import akka.http.scaladsl.server.Route
import com.neelnetwork.api.http._
import com.neelnetwork.http.BroadcastRoute
import com.neelnetwork.settings.RestAPISettings
import com.neelnetwork.utx.UtxPool
import io.netty.channel.group.ChannelGroup

case class AliasBroadcastApiRoute(settings: RestAPISettings, utx: UtxPool, allChannels: ChannelGroup) extends ApiRoute with BroadcastRoute {
  override val route = pathPrefix("alias" / "broadcast") {
    signedCreate
  }

  def signedCreate: Route = (path("create") & post) {
    json[SignedCreateAliasV1Request] { aliasReq =>
      doBroadcast(aliasReq.toTx)
    }
  }
}
