/**
 * Redirect all external links by prepending
 * "http://www.google.com/url?sa=D&q=" to it.  Call this function from the
 * onload() event handler on the body using something like: 
 *
 *   <body onload="redirectExternalLinks();">
 *
 * Works great on Linux/Firefox and Windows/Firefox.
 *
 * The rewritten link works on Windows/IE6 but it does something bizarre
 * with the text. If the text is identical to the href, then
 * when the href is rewritten, the text changes as well. If the text is
 * different from the href link, then the text does not change.
 * 
 * @author matts, bpark
 */
function redirectExternalLinks() {
  var redirect_prefix = "http://www.google.com/url?sa=D&q=";
  var re_http = /https?:/i;
  var re_google_com = /https?:\/\/[^\/]*google\.([a-zA-Z]{2,3}|(com?\.[a-zA-Z]{2}))[\/\?:$]/i;
  var re_simple_alias = /https?:\/\/[^\/\.]+[\/$]/i;
  var link_urls = document.links;
  for (var i = 0; i < link_urls.length; i++) {
    var this_href = link_urls[i].href;
    if (this_href.search(re_http) == 0 
        && this_href.search(re_google_com) != 0 
        && this_href.search(re_simple_alias) != 0) {
      var r_href = redirect_prefix + encodeURIComponent(this_href);
      link_urls[i].href = r_href;
    }
  }
}
