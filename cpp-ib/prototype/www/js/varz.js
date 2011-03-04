// varz.js
// Requires jquery
// All time in milliseconds

Varz = function(varzUrl, initVarz, varzParser) {
  this.varzUrl = varzUrl;
  this.varzParser = varzParser;
  this.varz = null;
  this.last_varz = initVarz;
};

Varz.NAME = "Varz";
Varz.VERSION = "0.1";
Varz.__repr__ = function() {
  return "[" + this.NAME + " " + this.VERSION + "]";
};

Varz.prototype.toString = function() {
  return this.__repr__();
};

Varz.prototype.sample = function() {
  var varz_json = $.ajax({
   url: this.varzUrl,
   crossDomain: true,
   async: false, dataType : 'json' }).responseText;

  if (varz_json != null) {
    var varzResp = $.parseJSON(varz_json);

    // Call the custom parser to deconstruct the varz into hash:
    this.varz = this.varzParser(this.last_varz, varzResp);
  } else {
    console.debug('No data!');
  }
};

Varz.prototype.plotData = function(time_key, key_array) {
  var data = [];
  data.push(new Date(this.varz[time_key]));
  for(var i in key_array) {
    var key = key_array[i];
    data.push(this.varz[key]);
  }
  this.last_varz = this.varz;
  return data;
};