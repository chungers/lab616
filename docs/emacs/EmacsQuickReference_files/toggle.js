/*****************************************************************************
 * 2 functions for toggling the display of a BLOCK-LEVEL element.
 * These functions do not work for toggling table cells or nested lists.
 *
 * TOG_toggle() just displays or hides a specified element. With no argument,
 * toggles the display of the element with ID=twiki_access. [Presumes the HTML
 * has some other way of indicating to the user what's going on.]
 *
 * TOG_toggleimg() both displays or hides a specificed element and changes the
 * source (image) of another element (typically, "this"). [Presumes that the
 * images somehow indicate hide/show.]
 *
 * Author: Victoria Gilbert <vpg@google.com>
 * Date: 2005-07-28
 *****************************************************************************/ 


/* TOG_toggle() displays or hides the element identified by whichID.
 * If supplied, whichID must be the ID of a block-level element.
 * With no argument, toggles the display of the element with ID=twiki_access.
 * Relies on the surrounding HTML to indicate to the reader what's going on.
 */

function TOG_toggle(whichID) {
  var taObj;
  
  if (whichID == null) {
    taObj = document.getElementById('twiki_access');
  } else {
    taObj = document.getElementById(whichID);
  }
  
  if (TOG_getDisplay(taObj) == 'block') {
    taObj.style.display = 'none';
  } else {    // "none" or ""
    taObj.style.display = 'block';
  }
}


/* 
 * TOG_toggleimg() changes the source image file used by imgObj (typically,
 * "this"). It also displays or hides the element identified by whichID.
 * whichID must be the ID of a block-level element.
 *
 * The optional 3rd and 4th elements are the images to toggle between. If not
 * specified, uses /img/open.gif and /img/closed.gif.
 *
 * This function assumes that the image is something that indicates open and
 * closedness, such as right and down facing triangles or + and - images.
 */

function TOG_toggleimg(imgObj, whichID, openI, closedI) {
  if (!openI) {openI = "/img/open.gif";}
  if (!closedI) {closedI = "/img/closed.gif";}

  var taObj = document.getElementById(whichID); // leave quietly if no obj

  if (TOG_getDisplay(taObj) == 'block') {
    taObj.style.display = 'none';
    imgObj.src = closedI;
  } else {    // "none" or ""
    taObj.style.display = 'block';
    imgObj.src = openI;
  }
}

/* Sadly, just checking the style attribute doesn't always cut it.
 * This function figures out what it really is... 
 */

function TOG_getDisplay(taObj) {
  var value = taObj.style.display;
  
  if (!value) {
    if (document.defaultView) {
      var computedStyle = document.defaultView.getComputedStyle(taObj, "");
      value = computedStyle.getPropertyValue('display');

    } else if (taObj.currentStyle) {
      value = taObj.currentStyle.display;
    }
  }
  
  return value;
}

