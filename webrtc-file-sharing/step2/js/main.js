'use strict';

navigator.getUserMedia = navigator.getUserMedia ||
  navigator.webkitGetUserMedia || navigator.mozGetUserMedia;

var constraints = {video: true};

function successCallback(stream) {
  window.stream = stream; // stream available to console
  var video = document.querySelector("video");
  video.src = window.URL.createObjectURL(stream);
  video.play();
}

function errorCallback(error){
  console.log("navigator.getUserMedia error: ", error);
}

navigator.getUserMedia(constraints, successCallback, errorCallback);
