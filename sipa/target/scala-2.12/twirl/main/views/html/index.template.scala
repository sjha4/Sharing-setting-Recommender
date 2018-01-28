
package views.html

import _root_.play.twirl.api.TwirlFeatureImports._
import _root_.play.twirl.api.TwirlHelperImports._
import _root_.play.twirl.api.Html
import _root_.play.twirl.api.JavaScript
import _root_.play.twirl.api.Txt
import _root_.play.twirl.api.Xml
import models._
import controllers._
import play.api.i18n._
import views.html._
import play.api.templates.PlayMagic._
import java.lang._
import java.util._
import scala.collection.JavaConverters._
import play.core.j.PlayMagicForJava._
import play.mvc._
import play.api.data.Field
import play.mvc.Http.Context.Implicit._
import play.data._
import play.core.j.PlayFormsMagicForJava._

object index extends _root_.play.twirl.api.BaseScalaTemplate[play.twirl.api.HtmlFormat.Appendable,_root_.play.twirl.api.Format[play.twirl.api.HtmlFormat.Appendable]](play.twirl.api.HtmlFormat) with _root_.play.twirl.api.Template0[play.twirl.api.HtmlFormat.Appendable] {

  /**/
  def apply/*1.2*/():play.twirl.api.HtmlFormat.Appendable = {
    _display_ {
      {


Seq[Any](format.raw/*1.4*/("""

"""),_display_(/*3.2*/main("SIPA")/*3.14*/ {_display_(Seq[Any](format.raw/*3.16*/("""
"""),format.raw/*4.1*/("""<form name="formFactory" action=""""),_display_(/*4.35*/routes/*4.41*/.HomeController.checkInfromForm()),format.raw/*4.74*/("""" method="post">
        <div id="content">
        	<div id="loc">
            <label>Enter Location</label></br>
            <select id="location" name = "location">
  			<option value="1">Graduation ceremony</option>
  			<option value="2">Library during the day</option>
 			<option value="3">Hurricane during the day</option>
 			<option value="4">Airport at night</option>
 			<option value="5">Hiking on a mountain</option>
 			<option value="6">Presenting a paper at a conference</option>
 			<option value="7">Bar with a fake ID</option>
			</select>
			</div>
			<div id="Friends">
			<br></br>
			<label>Enter Companion Ids separated by | </label></br>
            <textarea id=CompanionsId name="Companions" rows="15" cols="15"></textarea>
            </div>
            <div id="btn">
            <input id="submit_btn" name="submit_btn" type="submit" value="Submit" />
        	</div>
        </div>
    </form>
 
""")))}),format.raw/*29.2*/("""
"""))
      }
    }
  }

  def render(): play.twirl.api.HtmlFormat.Appendable = apply()

  def f:(() => play.twirl.api.HtmlFormat.Appendable) = () => apply()

  def ref: this.type = this

}


              /*
                  -- GENERATED --
                  DATE: Fri Nov 03 16:17:06 EDT 2017
                  SOURCE: C:/Users/samir/Desktop/3rd sem/Social Computing/SIPA/sipa/app/views/index.scala.html
                  HASH: f54e5af10c1f7bb0bb1dbccd159f0040fa09be04
                  MATRIX: 941->1|1037->3|1067->8|1087->20|1126->22|1154->24|1214->58|1228->64|1281->97|2265->1051
                  LINES: 28->1|33->1|35->3|35->3|35->3|36->4|36->4|36->4|36->4|61->29
                  -- GENERATED --
              */
          