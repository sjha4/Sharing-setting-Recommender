
// @GENERATOR:play-routes-compiler
// @SOURCE:C:/Users/samir/Desktop/3rd sem/Social Computing/S-d/SIPA/sipa/conf/routes
// @DATE:Sun Nov 12 23:03:17 EST 2017

import play.api.routing.JavaScriptReverseRoute


import _root_.controllers.Assets.Asset
import _root_.play.libs.F

// @LINE:7
package controllers.javascript {

  // @LINE:7
  class ReverseHomeController(_prefix: => String) {

    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:19
    def startApp: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.HomeController.startApp",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "startApp"})
        }
      """
    )
  
    // @LINE:21
    def checkInfromForm: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.HomeController.checkInfromForm",
      """
        function() {
          return _wA({method:"POST", url:"""" + _prefix + { _defaultPrefix } + """" + "checkInfromForm"})
        }
      """
    )
  
    // @LINE:17
    def getSanctionsForCheckins: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.HomeController.getSanctionsForCheckins",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "getSanctionsForCheckins"})
        }
      """
    )
  
    // @LINE:13
    def setCheckIn: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.HomeController.setCheckIn",
      """
        function(place0,companions1) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "setChekin/" + encodeURIComponent((""" + implicitly[play.api.mvc.PathBindable[String]].javascriptUnbind + """)("place", place0)) + "/" + encodeURIComponent((""" + implicitly[play.api.mvc.PathBindable[String]].javascriptUnbind + """)("companions", companions1))})
        }
      """
    )
  
    // @LINE:15
    def getCheckInAndReview: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.HomeController.getCheckInAndReview",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "getCheckInAndReview"})
        }
      """
    )
  
    // @LINE:7
    def index: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.HomeController.index",
      """
        function() {
          return _wA({method:"GET", url:"""" + _prefix + """"})
        }
      """
    )
  
  }

  // @LINE:11
  class ReverseAssets(_prefix: => String) {

    def _defaultPrefix: String = {
      if (_prefix.endsWith("/")) "" else "/"
    }

  
    // @LINE:11
    def versioned: JavaScriptReverseRoute = JavaScriptReverseRoute(
      "controllers.Assets.versioned",
      """
        function(file1) {
          return _wA({method:"GET", url:"""" + _prefix + { _defaultPrefix } + """" + "assets/" + (""" + implicitly[play.api.mvc.PathBindable[Asset]].javascriptUnbind + """)("file", file1)})
        }
      """
    )
  
  }


}
