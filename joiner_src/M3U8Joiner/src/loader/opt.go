package loader

import (
	"runtime"
)

type HttpOpts struct {
	Url     string
	Timeout int
	Header  map[string]string
}

func NewHttpOpts(url string) *HttpOpts {
	o := new(HttpOpts)
	o.Header = make(map[string]string)
	o.Timeout = 1500
	o.SetHeader("Accept", "*/*")
	o.SetHeader("UserAgent", "DWL/1.0.0 ("+runtime.GOOS+")")
	o.Url = url
	return o
}

func (o *HttpOpts) SetHeader(key, val string) {
	if o.Header == nil {
		o.Header = make(map[string]string)
	}
	o.Header[key] = val
}
