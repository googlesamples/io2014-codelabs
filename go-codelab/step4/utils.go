// This file provides some utility types and functions used by the rest of the
// code of the codelab.
// You don't need to modify anything in this file.

package todo

import (
	"bytes"
	"fmt"
	"io"
	"net/http"

	"appengine"
)

// appError is an error with a HTTP response code.
type appError struct {
	error
	Code int
}

// appErrorf creates a new appError given a reponse code and a message.
func appErrorf(code int, format string, args ...interface{}) *appError {
	return &appError{fmt.Errorf(format, args...), code}
}

// appHandler handles HTTP requests and manages returned errors.
type appHandler func(w io.Writer, r *http.Request) error

// appHandler implements http.Handler.
func (h appHandler) ServeHTTP(w http.ResponseWriter, r *http.Request) {
	c := appengine.NewContext(r)
	buf := &bytes.Buffer{}
	err := h(buf, r)
	if err == nil {
		io.Copy(w, buf)
		return
	}
	code := http.StatusInternalServerError
	logf := c.Errorf
	if err, ok := err.(*appError); ok {
		code = err.Code
		logf = c.Infof
	}

	w.WriteHeader(code)
	logf(err.Error())
	fmt.Fprint(w, err)
}
