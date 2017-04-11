package stats

import (
	"time"
)

type DownloadStat struct {
	Size       int64
	loadedByte int64

	deltaBytes int64
	speed      int64
	speedTime  time.Time

	isLoading      bool
	isLoadComplete bool
	IsComplete     bool
}

func (d *DownloadStat) StartSpeed() {
	d.speed = 0
	d.deltaBytes = 0
	d.loadedByte = 0
	d.speedTime = time.Now()
}

func (d *DownloadStat) StopSpeed() {
	d.speed = 0
	d.deltaBytes = 0
}

func (d *DownloadStat) GetSpeed() int64 {
	return d.speed
}

func (d *DownloadStat) GetLoadedBytes() int64 {
	return d.loadedByte
}

func (d *DownloadStat) IsLoading() bool {
	return d.isLoading
}

func (d *DownloadStat) SetLoading(val bool) {
	d.isLoading = val
}

func (d *DownloadStat) IsLoadComplete() bool {
	return d.isLoadComplete
}

func (d *DownloadStat) SetLoadComplete(val bool) {
	d.isLoadComplete = val
}

func (d *DownloadStat) MeasureSpeed(realc int) {
	d.deltaBytes += int64(realc)
	d.loadedByte += int64(realc)

	deltaTime := time.Since(d.speedTime).Seconds()
	if time.Since(d.speedTime).Seconds() > 0.1 {
		d.speed = int64(float64(d.deltaBytes) / deltaTime)
	}
	if time.Since(d.speedTime).Seconds() > 5 {
		d.speedTime = time.Now()
		d.deltaBytes = 0
	}
}
