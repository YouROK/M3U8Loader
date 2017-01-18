package m3u8

import (
	"crypto/aes"
	"crypto/cipher"
	"encoding/hex"
	"io/ioutil"
	"loader"
	"strings"

	"github.com/grafov/m3u8"
)

type Key struct {
	IV  []byte `json:",omitempty"`
	Key []byte `json:",omitempty"`
}

func loadM3U8Key(opts *loader.HttpOpts, m3u8key *m3u8.Key) (*Key, error) {
	http := loader.NewHttp(opts)
	err := http.Connect()
	if err != nil {
		return nil, err
	}
	buf, err := ioutil.ReadAll(http)
	http.Close()

	iv := m3u8key.IV
	var ivbuf []byte
	if iv != "" {
		if strings.HasPrefix(strings.ToLower(iv), "0x") {
			iv = iv[2:]
		}

		ivbuf, err = hex.DecodeString(iv)
		if err != nil {
			return nil, err
		}
	}
	k := new(Key)
	k.IV = ivbuf
	k.Key = buf

	return k, err
}

func decrypt(buf []byte, k *Key) error {
	block, err := aes.NewCipher(k.Key)
	if err != nil {
		return err
	}
	mode := cipher.NewCBCDecrypter(block, k.IV)
	mode.CryptBlocks(buf, buf)
	return nil
}
