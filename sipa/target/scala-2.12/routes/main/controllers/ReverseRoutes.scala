
// @GENERATOR:play-routes-compiler
// @SOURCE:C:/Users/samir/Desktop/3rd sem/Social Computing/S-d/SIPA/sipa/conf/routes
// @DATE:Sun Nov 12 23:03:17 EST 2017

import play.api.mvc.Call


import _root_.controllers.Assets.Asset
import _root_.play.libs.F

// @LINE:7
package controllers {

  // @LINE:7
  class ReverseHomeController(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:19
    def startApp(): Call = {
      
      Call("GET", _prefix + { _defaultPrefix } + "startApp")
    }
  
    // @LINE:21
    def checkInfromForm(): Call = {
      
      Call("POST", _prefix + { _defaultPrefix } + "checkInfromForm")
    }
  
    // @LINE:17
    def getSanctionsForCheckins(): Call = {
      
      Call("GET", _prefix + { _defaultPrefix } + "getSanctionsForCheckins")
    }
  
    // @LINE:13
    def setCheckIn(place:String, companions:String): Call = {
      
      Call("GET", _prefix + { _defaultPrefix } + "setChekin/" + play.core.routing.dynamicString(implicitly[play.api.mvc.PathBindable[String]].unbind("place", place)) + "/" + play.core.routing.dynamicString(implicitly[play.api.mvc.PathBindable[String]].unbind("companions", companions)))
    }
  
    // @LINE:15
    def getCheckInAndReview(): Call = {
      
      Call("GET", _prefix + { _defaultPrefix } + "getCheckInAndReview")
    }
  
    // @LINE:7
    def index(): Call = {
      
      Call("GET", _prefix)
    }
  
  }

  // @LINE:11
  class ReverseAssets(_prefix: => String) {
    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:11
    def versioned(file:Asset): Call = {
      implicit lazy val _rrc = new play.core.routing.ReverseRouteContext(Map(("path", "/public"))); _rrc
      Call("GET", _prefix + { _defaultPrefix } + "assets/" + implicitly[play.api.mvc.PathBindable[Asset]].unbind("file", file))
    }
  
  }


}
