let map_x, map_y, map_w, map_h;
var highlited_regions = [];

function draw_map_preview() {
	fill(255);
	map_x = DEFAULT_WIDTH-MAP_PREVIEW_WIDTH-CATALOGUE_SCROLLER_WIDTH;
	map_y = 100;
	map_w = MAP_PREVIEW_WIDTH;
	map_h = MAP_PREVIEW_WIDTH;
	image(map_img, map_x, map_y, map_w, map_h);
	let tag_map = '';
	if((lan === 'chn')) {textFont(font_chn);}
	else textFont('Arial');
	for(let i = 0; i < highlited_regions.length; i++) {
		if(map_regions_map.has(highlited_regions[i])) {
			image(map_regions_map.get(highlited_regions[i]), map_x, map_y, map_w, map_h);
		}
		fill(255);
		textAlign(CENTER, TOP);
		textSize(18);
		let new_tag = highlited_regions[i];
		if((lan === 'chn')) {
			new_tag = REGIONS_CHN.get(new_tag);
		}else{
			if(new_tag === 'Western Europe') new_tag = 'Paris';
			else if(new_tag === 'Soviet Union') new_tag = 'Moscow';
		}
		tag_map = tag_map.concat(new_tag).concat(', ');
	}
	tag_map = tag_map.substring(0, tag_map.length-2);
	if(state != 3) //State 3 has its own marking for geographical regions
		text(tag_map, map_x+map_w/2, map_y+map_h);
}

function draw_region_preview(regions) {
	for(let i = 0; i < regions.length; i++) {
		let region = regions[i];
		if(region.includes('Shanghai')) region = 'Shanghai';
		if(region.includes('Inner Mongolia')) region = 'Inner_Mongolia';
		if(region.includes("Xi'an")) region = 'Shaanxi';
		if(region.includes("Northeast")) region = 'Jilin';
		if(region.includes("Innter Mongolia")) region = 'Inner_Mongolia';
		if(!highlited_regions.includes(region)) highlited_regions.push(region);
	}
}