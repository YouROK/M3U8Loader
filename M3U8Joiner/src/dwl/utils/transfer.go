package utils

import (
	"dwl/client"
	"io"
	"net/http"
)

func ReadBuf(Url string, header http.Header) ([]byte, error) {
	cli, err := client.GetClient(Url, header)
	if err != nil {
		return nil, err
	}

	err = cli.Connect()
	if err != nil {
		return nil, err
	}

	bufsize := 65535
	buffer := make([]byte, bufsize)
	out := make([]byte, 0)
	for {
		n, err := cli.Read(buffer)
		if n > 0 {
			out = append(out, buffer[:n]...)
		}
		if err == io.EOF {
			err = nil
			break
		}

		if err != nil {
			return nil, err
		}
	}
	return out, nil
}
