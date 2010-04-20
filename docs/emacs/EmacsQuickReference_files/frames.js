// Wiki frame functions, used when the Wiki page is embedded in another page.
//
// Originally created for the integration with new PDB.
//
// Copyright 2007 Google Inc. All rights reserved.
//
// Author: mwichary@google.com (Marcin Wichary)

// if document.domain is ever set, it must be set before the hovercard
// js is loaded (the hovercard DOES NOT require document.domain to be
// set, but setting document.domain to a value after the hovercard js
// is loaded but before the hovercard js is used causes issues in IE)
document.domain = 'corp.google.com';

// If the page is embedded within another page, add a class to <body>
// that will hide certain elements (header) and show others.
// Also, show or hide header depending on the cookie option.
if (top.location != location) {
  document.body.className += " embedded";
  if (document.cookie.indexOf("noHeader=false") == -1) {
    document.body.className += " noHeader";
  }
}

// Internet Explorer, quite typically, needs special attention
browserIsIE = ((navigator.userAgent.indexOf("MSIE") > -1) && 
               (navigator.userAgent.indexOf("Opera") == -1));
browserIsSafari = (navigator.userAgent.indexOf("KHTML") > -1);
browserIsOpera = (navigator.userAgent.indexOf("Opera") > -1);

/**
 * Shows or hides the header depending on its current state.
 * This is invoked by the user by clicking on a link in the upper right
 * corner (visible only when the page is embedded within another page).
 * The preference will be saved in a cookie. 
 */   
function toggleHeader() {
  if (document.body.className.indexOf("noHeader") != -1) {
    document.body.className = document.body.className.replace(/noHeader/, "");
    var cookieValue = "false";
  } else {
    document.body.className += " noHeader";
    var cookieValue = "true";
  }

  var date = new Date();
  date.setTime(date.getTime() + 10 * 365 * 24 * 60 * 60 * 1000); // 10 years
  document.cookie = "noHeader=" + cookieValue + 
                    "; expires=" + date.toGMTString() +
                    "; path=/";

  parentResizeIframeToFitContent();

  return false;
}

/**
 * Breaks out of any frames and iframes the Wiki page might contained in.
 * This is invoked by the user by clicking on a link in the upper right
 * corner (visible only when the page is embedded within another page).
 */ 
function expandFrameToEntirePage() {
  top.location.href = document.location.href;
}

/**
 * This asks the parent to resize the iframe to show all the content without
 * scrollbars (vertical, at least).
 *
 * The parent document should have its document.domain set to 
 * 'corp.google.com', and the frame should have its id set to 'resizableIframe'
 *
 * TODO(mwichary): add polling here so the iframe will continuously
 * adjust to the content (e.g. when people open/close zippies, change
 * font sizes, etc.), instead of just on resize
 */
function parentResizeIframeToFitContent() {
  // We're setting document.domain so that we can talk between the iframe
  // and the document. This should be set to the same value on the other end
  document.domain = 'corp.google.com';

  try {
    if (parent && parent.document && 
        parent.document.getElementById('resizableIframe')) {

      // This prevents a loop where resizing an iframe to fit content will trigger
      // an event in content that will ask an iframe to resize... etc.  
      if (document.ignoreNextParentResizeIframeToFitContent) {
        document.ignoreNextParentResizeIframeToFitContent = false;
        return;
      } 

      // TODO(mwichary): In Safari/Opera, if you change the height of the iframe 
      // to be bigger than height of the body inside, the body will "adopt" new 
      // height as its own. If you change the width to be very narrow (and, by 
      // extension, very tall), and then expand to be wide again, the extreme 
      // height will still "stick." I don't yet know of a way to help with that.

      if (browserIsIE) {
        var height = document.body.scrollHeight;
      } else {
        var height = document.documentElement.scrollHeight; 
      }

      // Adding 1px will cause the height to grow indefinitely as you're resizing the
      // window in Safari/Opera. We don't want that. (We're adding 1px to prevent
      // the scrollbar from appearing in IE/Firefox. Note that it doesn't help in all
      // the cases.) 

      if (!browserIsSafari && !browserIsOpera)  {
        height++;
      }

      parent.document.getElementById('resizableIframe').style.height = height + "px";

      document.ignoreNextParentResizeIframeToFitContent = true;
    }
  } catch(e) {

  }
}
