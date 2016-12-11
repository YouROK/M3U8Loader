package main

import (
	"fmt"
	"log"
	"m3u8"
	"time"
)

func main() {

	opt := m3u8.NewOptions()
	opt.TempDir = "/home/yourok/tmp/"
	//	opt.Url = "file:///home/yourok/test.m3u"
	opt.Url = "http://atv-vod.ercdn.net/eskiya_dunyaya_hukumdar_olmaz/050/eskiya_dunyaya_hukumdar_olmaz_050.smil/chunklist_b400000_t64NDAwa2Jwcw==.m3u8?st=BcClLVYdRNCZwxdFnh7TEw&e=1481524980&SessionID=5fmkqtl3v42jtuw4pipfxacm&StreamGroup=eskiya-dunyaya-hukumdar-olmaz&Site=atv&DeviceGroup=web"
	opt.Name = "test"
	opt.OutFileDir = opt.TempDir
	opt.Threads = 30
	opt.Timeout = 20000

	m := m3u8.NewM3U8(opt)
	go func() {
		err := m.LoadList()
		if err == nil {
			fmt.Println("Load")
			err = m.Load()
			if err == nil {
				fmt.Println("Join")
				err = m.Join()
			}
		}
		if err != nil {
			fmt.Println(err)
		}
	}()
	time.Sleep(time.Millisecond * 2000)
	//	stoped := 0
	log.Println("receive state")

	for m.IsLoading() || m.IsJoin() {
		log.Println(m3u8.GetState(m))
		time.Sleep(time.Millisecond * 1000)
	}
}
