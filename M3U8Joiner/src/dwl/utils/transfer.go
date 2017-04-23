package utils

import (
	"dwl/client"
	"errors"
	"io"
	"net/http"
)

func ReadBufText(Url string, header http.Header) ([]byte, error) {
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
			ibs := IsBinarySafe(buffer[:n])
			if ibs > 1 {
				err = errors.New(Url + ", wrong data format, must be text")
			}
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

func ReadBufBinary(Url string, header http.Header) ([]byte, error) {
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
