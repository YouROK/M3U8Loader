package main

import (
	"dwl"
	"dwl/load"
	"dwl/utils"
	"fmt"
	"time"
)

func main() {

	//url := "http://localhost:8090/files/bipbop/gear4/prog_index.m3u8"
	//url := "http://localhost:8090/files/bipbop/bipbopall.m3u8"
	url := "https://devimages.apple.com.edgekey.net/streaming/examples/bipbop_4x3/gear1/prog_index.m3u8"
	//url := "http://localhost:8090/files/crypted/crypted.m3u8"
	name := "test"

	manager, err := dwl.OpenManager("/home/yourok/tmp/video/config/")
	if err != nil {
		fmt.Println("Error open manager", err)
	}

	manager.SetSettingsThreads(100)
	manager.SetSettingsDownloadPath("/home/yourok/tmp/video/")
	manager.SetSettingsDynamicSize(true)
	manager.SaveSettings()

	if manager.Len() == 0 {
		err = manager.Add(url, name, "", "") //"Token=1492582676; Service=proxsee; Digest=ToHLzuPSFh9788bM-4tSQsk6_AlgYDODLNv_UldQ7mE", "")
		if err != nil {
			fmt.Println("Error add:", err)
			return
		}
	} else {
		manager.Clean(0)
	}

	manager.Load(0)
	if manager.GetLoaderInfo(0).Status == load.STATUS_STOPED {
		for {
			if manager.GetLoaderInfo(0).Status != load.STATUS_STOPED {
				break
			}
			time.Sleep(time.Second)
		}
	}

	//go func() {
	//	time.Sleep(time.Second * 5)
	//	manager.Stop(0)
	//	time.Sleep(time.Second * 1)
	//	manager.Load(0)
	//}()

	fmt.Println()
	fmt.Println("Url:", manager.GetLoaderInfo(0).Url)
	fmt.Println("Name:", manager.GetLoaderInfo(0).Name)
	fmt.Println("Size:", utils.ByteSize(manager.GetLoaderInfo(0).LoadedBytes))
	go func() {
		for {

			info := manager.GetLoaderInfo(0)
			st := info.Status
			fmt.Println("Status:", st)
			fmt.Println("Threads:", info.Threads)
			fmt.Println("Speed:", utils.ByteSize(info.Speed))
			fmt.Println("Loaded:", info.Completed, "/", info.LoadingCount, info.Completed*100/info.LoadingCount, "% ", utils.ByteSize(info.LoadedBytes))
			fmt.Println("Duration:", utils.TimeSize(info.LoadedDuration), "/", utils.TimeSize(info.Duration))
			fmt.Println()
			if st == load.STATUS_COMPLETE {
				fmt.Println("Complete")
			}
			if st == load.STATUS_ERROR {
				fmt.Println("Error load:", info.Error)
			}
			if st == load.STATUS_STOPED {
				fmt.Println("Load stoped")
			}
			time.Sleep(time.Second)
			fmt.Println()
		}
	}() /**/
	for !manager.WaitLoader(0, 2000) {
		fmt.Println("TIMEOUT")
	}
	fmt.Println("FINISH")
	time.Sleep(time.Second * 2)
}
