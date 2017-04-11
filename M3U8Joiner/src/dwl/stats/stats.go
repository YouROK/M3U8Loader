package stats

import (
	"dwl/utils"
	"fmt"
	"time"
)

func Test() {
	ds := new(DownloadStat)

	i := 0

	go func() {
		for true {
			fmt.Println(utils.ByteSize(ds.GetSpeed()))
			time.Sleep(time.Millisecond * 100)
			i++
		}
	}()
	ds.StartSpeed()
	for i := 0; i < 1000; i++ {
		ds.MeasureSpeed(10 * 1024)
		time.Sleep(time.Millisecond * 100)
	}
	ds.StopSpeed()
}
