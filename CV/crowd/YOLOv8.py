import cv2
import glob
import numpy as np

from ultralytics import YOLO
from ultralytics.yolo.utils.plotting import Annotator
from tqdm import tqdm

import datetime

TIME_INTERVAL = 5.0 # second(s) for down-sampling
PEOPLE_THRESHOLD = 10 # number of people in a frame to be considered as a crowd
CONF_THRESHOLD = 0.05

RUN_ON_FOUND_FRAMES = True # if True, only run on frames that have been found to have crowds and save the faces and persons' box information

model = YOLO("CV/models/yolov8m.pt") 
videos = []

def processVideo(pathIn, pathOut, thumbPath=None):
	VIDEO_NAME = pathIn.split('/')[-1]
	if VIDEO_NAME.endswith('.mp4') or VIDEO_NAME.endswith('.m4v'):
		VIDEO_NAME = VIDEO_NAME[:-4]

	if not RUN_ON_FOUND_FRAMES:
		with open('CV/crowd/processed_videos.csv', 'r') as f_object:
			text = f_object.readline()
			while text != "":
					text = f_object.readline()
					v_name = text.split(',')[0]
					if v_name == VIDEO_NAME:
						f_object.close()
						return #skip processed videos
			f_object.close()

	count = 0
	vidcap = cv2.VideoCapture(pathIn)
	success,image = vidcap.read()
	success = True
	total_secs = vidcap.get(cv2.CAP_PROP_FRAME_COUNT) / vidcap.get(cv2.CAP_PROP_FPS)

	now = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")
	print(f"Processing {VIDEO_NAME} with {total_secs} seconds ({now})")
	p_bar = tqdm(total=total_secs)

	while success:
		pos = count*TIME_INTERVAL # seconds
		if pos > total_secs:
			break
		
		vidcap.set(cv2.CAP_PROP_POS_MSEC, pos * 1000)
		p_bar.update(TIME_INTERVAL)
		success,image = vidcap.read()
		
		if success:
			run_this_frame = True

			if RUN_ON_FOUND_FRAMES:
				run_this_frame = False
				with open("CV/crowd/found_frames-Soviet.csv", 'r') as f_object:
					text = f_object.readline()
					while text != "":
						text = f_object.readline()
						v_name = text.split(',')[0]
						if(v_name != VIDEO_NAME): continue

						fr = int(text.split(',')[1])
						if v_name == VIDEO_NAME and fr == count:
							run_this_frame = True
							break
					f_object.close()

			if run_this_frame:
				person_in_frame, thumb, normed_boxes, confs  = get_person_count_in_image(image)
				
				if not RUN_ON_FOUND_FRAMES:
					if person_in_frame > PEOPLE_THRESHOLD:
						cv2.imwrite(pathOut + f"/{VIDEO_NAME}-{count}_{person_in_frame}.jpg", image)
						#print(f"Found a crowd with {person_in_frame} detectable persons in {VIDEO_NAME} at {count} seconds")
						with open('CV/crowd/found_frames-Soviet.csv', 'a') as f_object:
							f_object.write("{},{},{}\n".format(VIDEO_NAME, count, person_in_frame))
							f_object.close()
						if thumb is not None and thumbPath is not None:
							cv2.imwrite(thumbPath + f"/{VIDEO_NAME}-t{count}_{person_in_frame}.jpg", thumb)
				else:
					cv2.imwrite(pathOut + f"/{VIDEO_NAME}-{count}_{person_in_frame}.jpg", image)
					with open('CV/crowd/boxes-Soviet.csv', 'a') as box_out:
						for i in range(len(normed_boxes)):
							norm_b = normed_boxes[i]
							conf = confs[i]
							box_out.write(f'{VIDEO_NAME},{count},{norm_b[0]},{norm_b[1]},{norm_b[2]},{norm_b[3]},{conf}\n')
						box_out.close()
		
		count = count + 1
    
	p_bar.close()
	vidcap.release()
	with open('CV/crowd/processed_videos.csv', 'a') as f_object:
		f_object.write(f"{VIDEO_NAME},{count}\n")
		f_object.close()
	
def get_person_count_in_image(img):
	results = model(img, device = "mps", verbose=False)  # predict on an image
	number = 0
	thumb = img.copy()

	normed_boxes = []
	confs = []

	for r in results:
		annotator = Annotator(thumb)
		boxes = r.boxes
		for box in boxes:
			b = box.xyxy[0]  # get box coordinates in (top, left, bottom, right) format
			norm_b = box.xyxyn[0]
			c = box.cls
			conf = box.conf.item()
			if c != 0:
				# not a person, skip
				continue
			if conf < CONF_THRESHOLD:
				# not confident, skip
				continue

			normed_boxes.append(norm_b)
			confs.append(conf)
			annotator.box_label(b)
			number += 1
	thumb = annotator.result()
	thumb = cv2.resize(thumb, (0,0), fx=0.5, fy=0.5)

	return number, thumb, normed_boxes, confs

for filename in glob.glob('../non-chn-films/*.m4v'):
	processVideo(filename, 'CV/Temp', 'CV/thumbnails/cache')