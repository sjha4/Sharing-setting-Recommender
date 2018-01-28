
// @GENERATOR:play-routes-compiler
// @SOURCE:C:/Users/samir/Desktop/3rd sem/Social Computing/S-d/SIPA/sipa/conf/routes
// @DATE:Sun Nov 12 23:03:17 EST 2017

package router

import play.core.routing._
import play.core.routing.HandlerInvokerFactory._

import play.api.mvc._

import _root_.controllers.Assets.Asset
import _root_.play.libs.F

class Routes(
  override val errorHandler: play.api.http.HttpErrorHandler, 
  // @LINE:7
  HomeController_0: controllers.HomeController,
  // @LINE:11
  Assets_1: controllers.Assets,
  val prefix: String
) extends GeneratedRouter {

   @javax.inject.Inject()
   def this(errorHandler: play.api.http.HttpErrorHandler,
    // @LINE:7
    HomeController_0: controllers.HomeController,
    // @LINE:11
    Assets_1: controllers.Assets
  ) = this(errorHandler, HomeController_0, Assets_1, "/")

  def withPrefix(prefix: String): Routes = {
    router.RoutesPrefix.setPrefix(prefix)
    new Routes(errorHandler, HomeController_0, Assets_1, prefix)
  }

  private[this] val defaultPrefix: String = {
    if (this.prefix.endsWith("/")) "" else "/"
  }

  def documentation = List(
    ("""GET""", this.prefix, """controllers.HomeController.index"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """assets/""" + "$" + """file<.+>""", """controllers.Assets.versioned(path:String = "/public", file:Asset)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """setChekin/""" + "$" + """place<[^/]+>/""" + "$" + """companions<[^/]+>""", """controllers.HomeController.setCheckIn(place:String, companions:String)"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """getCheckInAndReview""", """controllers.HomeController.getCheckInAndReview()"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """getSanctionsForCheckins""", """controllers.HomeController.getSanctionsForCheckins()"""),
    ("""GET""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """startApp""", """controllers.HomeController.startApp()"""),
    ("""POST""", this.prefix + (if(this.prefix.endsWith("/")) "" else "/") + """checkInfromForm""", """controllers.HomeController.checkInfromForm()"""),
    Nil
  ).foldLeft(List.empty[(String,String,String)]) { (s,e) => e.asInstanceOf[Any] match {
    case r @ (_,_,_) => s :+ r.asInstanceOf[(String,String,String)]
    case l => s ++ l.asInstanceOf[List[(String,String,String)]]
  }}


  // @LINE:7
  private[this] lazy val controllers_HomeController_index0_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix)))
  )
  private[this] lazy val controllers_HomeController_index0_invoker = createInvoker(
    HomeController_0.index,
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.HomeController",
      "index",
      Nil,
      "GET",
      this.prefix + """""",
      """ An example controller showing a sample home page""",
      Seq("""nocsrf""")
    )
  )

  // @LINE:11
  private[this] lazy val controllers_Assets_versioned1_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("assets/"), DynamicPart("file", """.+""",false)))
  )
  private[this] lazy val controllers_Assets_versioned1_invoker = createInvoker(
    Assets_1.versioned(fakeValue[String], fakeValue[Asset]),
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.Assets",
      "versioned",
      Seq(classOf[String], classOf[Asset]),
      "GET",
      this.prefix + """assets/""" + "$" + """file<.+>""",
      """ Map static resources from the /public folder to the /assets URL path""",
      Seq("""nocsrf""")
    )
  )

  // @LINE:13
  private[this] lazy val controllers_HomeController_setCheckIn2_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("setChekin/"), DynamicPart("place", """[^/]+""",true), StaticPart("/"), DynamicPart("companions", """[^/]+""",true)))
  )
  private[this] lazy val controllers_HomeController_setCheckIn2_invoker = createInvoker(
    HomeController_0.setCheckIn(fakeValue[String], fakeValue[String]),
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.HomeController",
      "setCheckIn",
      Seq(classOf[String], classOf[String]),
      "GET",
      this.prefix + """setChekin/""" + "$" + """place<[^/]+>/""" + "$" + """companions<[^/]+>""",
      """""",
      Seq("""nocsrf""")
    )
  )

  // @LINE:15
  private[this] lazy val controllers_HomeController_getCheckInAndReview3_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("getCheckInAndReview")))
  )
  private[this] lazy val controllers_HomeController_getCheckInAndReview3_invoker = createInvoker(
    HomeController_0.getCheckInAndReview(),
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.HomeController",
      "getCheckInAndReview",
      Nil,
      "GET",
      this.prefix + """getCheckInAndReview""",
      """""",
      Seq("""nocsrf""")
    )
  )

  // @LINE:17
  private[this] lazy val controllers_HomeController_getSanctionsForCheckins4_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("getSanctionsForCheckins")))
  )
  private[this] lazy val controllers_HomeController_getSanctionsForCheckins4_invoker = createInvoker(
    HomeController_0.getSanctionsForCheckins(),
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.HomeController",
      "getSanctionsForCheckins",
      Nil,
      "GET",
      this.prefix + """getSanctionsForCheckins""",
      """""",
      Seq("""nocsrf""")
    )
  )

  // @LINE:19
  private[this] lazy val controllers_HomeController_startApp5_route = Route("GET",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("startApp")))
  )
  private[this] lazy val controllers_HomeController_startApp5_invoker = createInvoker(
    HomeController_0.startApp(),
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.HomeController",
      "startApp",
      Nil,
      "GET",
      this.prefix + """startApp""",
      """""",
      Seq("""nocsrf""")
    )
  )

  // @LINE:21
  private[this] lazy val controllers_HomeController_checkInfromForm6_route = Route("POST",
    PathPattern(List(StaticPart(this.prefix), StaticPart(this.defaultPrefix), StaticPart("checkInfromForm")))
  )
  private[this] lazy val controllers_HomeController_checkInfromForm6_invoker = createInvoker(
    HomeController_0.checkInfromForm(),
    play.api.routing.HandlerDef(this.getClass.getClassLoader,
      "router",
      "controllers.HomeController",
      "checkInfromForm",
      Nil,
      "POST",
      this.prefix + """checkInfromForm""",
      """""",
      Seq("""nocsrf""")
    )
  )


  def routes: PartialFunction[RequestHeader, Handler] = {
  
    // @LINE:7
    case controllers_HomeController_index0_route(params@_) =>
      call { 
        controllers_HomeController_index0_invoker.call(HomeController_0.index)
      }
  
    // @LINE:11
    case controllers_Assets_versioned1_route(params@_) =>
      call(Param[String]("path", Right("/public")), params.fromPath[Asset]("file", None)) { (path, file) =>
        controllers_Assets_versioned1_invoker.call(Assets_1.versioned(path, file))
      }
  
    // @LINE:13
    case controllers_HomeController_setCheckIn2_route(params@_) =>
      call(params.fromPath[String]("place", None), params.fromPath[String]("companions", None)) { (place, companions) =>
        controllers_HomeController_setCheckIn2_invoker.call(HomeController_0.setCheckIn(place, companions))
      }
  
    // @LINE:15
    case controllers_HomeController_getCheckInAndReview3_route(params@_) =>
      call { 
        controllers_HomeController_getCheckInAndReview3_invoker.call(HomeController_0.getCheckInAndReview())
      }
  
    // @LINE:17
    case controllers_HomeController_getSanctionsForCheckins4_route(params@_) =>
      call { 
        controllers_HomeController_getSanctionsForCheckins4_invoker.call(HomeController_0.getSanctionsForCheckins())
      }
  
    // @LINE:19
    case controllers_HomeController_startApp5_route(params@_) =>
      call { 
        controllers_HomeController_startApp5_invoker.call(HomeController_0.startApp())
      }
  
    // @LINE:21
    case controllers_HomeController_checkInfromForm6_route(params@_) =>
      call { 
        controllers_HomeController_checkInfromForm6_invoker.call(HomeController_0.checkInfromForm())
      }
  }
}
