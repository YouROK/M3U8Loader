package crypto

import (
	"crypto/aes"
	"crypto/cipher"
)

type Key struct {
	IV  []byte `json:",omitempty"`
	Key []byte `json:",omitempty"`
}

func Decrypt(buf []byte, k *Key) error {
	block, err := aes.NewCipher(k.Key)
	if err != nil {
		return err
	}
	mode := cipher.NewCBCDecrypter(block, k.IV)
	mode.CryptBlocks(buf, buf)
	return nil
}
