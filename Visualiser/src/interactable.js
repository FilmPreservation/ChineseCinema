const PRESS_SCALE = 0.9;

class Interactable {
	constructor(x, y, width, height, image = null, image_scale = 1, caption_chn = "", caption_eng = "", rect_fill = color(255), caption_size = 20, ignore_offset = false) {
		this.x = x;
		this.y = y;
		if(image == null) {
			this.width = width;
			this.height = height;
		}else {
			this.image = image;
			this.width = this.image.width * image_scale;
			this.height = this.image.height * image_scale;
		}
		this.image = this.image;
		this.inactive = false;
		this.caption_chn = caption_chn;
		this.caption_eng = caption_eng;
		this.centreX = x + width / 2;
		this.centreY = y + height / 2;
		this.caption_size = caption_size;
		this.rect_fill = rect_fill;
		this.ignore_offset = ignore_offset;
	}

	isSelected() {
		let realCursorX = cursorX + (this.ignore_offset ? frame_offset_x : 0);
		let realCursorY = cursorY + (this.ignore_offset ? frame_offset_y : 0);
		if(realCursorX > this.x && realCursorX < this.x + this.width) {
			if(realCursorY > this.y && realCursorY < this.y + this.height) {
				return true;
			}
		}
		return false;
	}

	offscreen() {
		if(this.ignore_offset) return false;
		else return this.x < -this.width*2-frame_offset_x || this.y < -this.height*2-frame_offset_y || this.x > DEFAULT_WIDTH+this.width-frame_offset_x || this.y > DEFAULT_HEIGHT+this.height-frame_offset_y;
	}
	
	update() {
		if(this.offscreen()) {
			return;
		}

		this.preUpdate();

		fill(255);
		if(this.inactive) {
			if(this.image != null) {
				tint(220, 120, 0);
				image(this.image, this.x, this.y, this.width, this.height);
				noTint();
			}else{
				fill(this.rect_fill);
				rect(this.x, this.y, this.width, this.height, 10);
			}
		}else if(this.isSelected()) {
			this.hover();

			push();
			let tempW = this.width;
			let tempH = this.height;
			let tintVal = 0;
			if(clicked) {
				tintVal = 160;
				tempW = (int) (this.width * PRESS_SCALE);
				tempH = (int) (this.height * PRESS_SCALE);
			}else {
				tintVal = 220;
				if(pClicked) {
					this.interact();
				}
			}
			if(this.image != null) {
				tint(tintVal);
				image(this.image, this.x + (this.width - tempW) / 2, this.y + (this.height - tempH) / 2, tempW, tempH);
				noTint();
			}else {
				fill(getDarkerColour(this.rect_fill, parseFloat(tintVal)/255));
				rect(this.x, this.y, this.width, this.height, 10);
			}
			pop();
		}else {
			if(this.image != null) {
				image(this.image, this.x, this.y, this.width, this.height);
			}else {
				fill(this.rect_fill);
				rect(this.x, this.y, this.width, this.height, 10);
			}
		}
		textAlign(CENTER, CENTER);
		textSize(this.caption_size);
		fill(0);
		text((lan === 'chn' ? this.caption_chn : this.caption_eng), this.centreX, this.centreY);
		textAlign(LEFT, TOP);
	}
	
	interact() { }
	hover() { }
	preUpdate() { }
}

class FilmSelectButton extends Interactable {
	constructor(x, y, width, height, image = null, image_scale = 1, film_key = "", caption_chn = "", caption_eng = "", rect_fill = color(255), caption_size = 20) {
		super(x, y, width, height, image, image_scale, caption_chn, caption_eng, rect_fill, caption_size, false);
		this.film_key = film_key;
	}
	
	interact() {
		if(!this.inactive) {
			show_film_page(this.film_key);
			state = 5;
		}
	}

	preUpdate() {
		if(cursorY + frame_offset_y > DEFAULT_HEIGHT - CATALOGUE_BOTTOM_BAR_HEIGHT) {
			this.inactive = true;
		}else{
			this.inactive = false;
		}
	}

	hover() {
		draw_region_preview(filmMap.get(this.film_key).category);
	}
}

class SearchButton extends Interactable {
	constructor(x, y, width, height, image = null, image_scale = 1, caption_chn = "", caption_eng = "", rect_fill = color(255), caption_size = 20) {
		super(x, y, width, height, image, image_scale, caption_chn, caption_eng, rect_fill, caption_size, true);
	}
	
	interact() {
		if(!this.inactive) {
			search_mode = 0;
			search_in_catalogue(search_field.value());
			print(search_field.value());
		}
	}
}

class ClearSearchButton extends Interactable {
	constructor(x, y, width, height, image = null, image_scale = 1, caption_chn = "", caption_eng = "", rect_fill = color(255), caption_size = 20) {
		super(x, y, width, height, image, image_scale, caption_chn, caption_eng, rect_fill, caption_size, true);
	}
	
	interact() {
		if(!this.inactive) {
			search_mode = 0;
			search_field.value('');
			if(search_input.length > 0)
				search_in_catalogue('');
		}
	}
}

class BackToCatalogueButton extends Interactable {
	constructor(x, y, width, height, image = null, image_scale = 1, caption_chn = "", caption_eng = "", rect_fill = color(255), caption_size = 20) {
		super(x, y, width, height, image, image_scale, caption_chn, caption_eng, rect_fill, caption_size, true);
	}
	
	interact() {
		if(!this.inactive) {
			state = 1;
			show_all_films_in_catalogue();
		}
	}
}

class CloseFilmInfoStaffPageButton extends Interactable {
	constructor(x, y, width, height, image = null, image_scale = 1, caption_chn = "", caption_eng = "", rect_fill = color(255), caption_size = 20) {
		super(x, y, width, height, image, image_scale, caption_chn, caption_eng, rect_fill, caption_size, true);
	}
	
	interact() {
		if(!this.inactive) {
			showing_ohter_staff_page = false;
		}
	}
}

class OpenFilmInfoStaffPageButton extends Interactable {
	constructor(x, y, width, height, image = null, image_scale = 1, caption_chn = "", caption_eng = "", rect_fill = color(255), caption_size = 20) {
		super(x, y, width, height, image, image_scale, caption_chn, caption_eng, rect_fill, caption_size, true);
	}
	
	interact() {
		if(!this.inactive) {
			showing_ohter_staff_page = true;
		}
	}
}

class NameSearchButton extends Interactable {
	constructor(x, y, width, height, image = null, image_scale = 1, caption_chn = "", caption_eng = "", rect_fill = color(255), caption_size = 20) {
		super(x, y, width, height, image, image_scale, caption_chn, caption_eng, rect_fill, caption_size, true);
	}
	
	interact() {
		if(!this.inactive) {
			search_in_names(search_field.value());
		}
	}
}

class ClearNameSearchButton extends Interactable {
	constructor(x, y, width, height, image = null, image_scale = 1, caption_chn = "", caption_eng = "", rect_fill = color(255), caption_size = 20) {
		super(x, y, width, height, image, image_scale, caption_chn, caption_eng, rect_fill, caption_size, true);
	}
	
	interact() {
		if(!this.inactive) {
			search_field.value('');
			if(name_search_input.length > 0)
				search_in_names('');
		}
	}
}

class NameSelectButton extends Interactable {
	constructor(x, y, width, height, image = null, image_scale = 1, name_key = "", caption_chn = "", caption_eng = "", rect_fill = color(255), caption_size = 20) {
		super(x, y, width, height, image, image_scale, caption_chn, caption_eng, rect_fill, caption_size, false);
		this.name_key = name_key;
	}
	
	interact() {
		if(!this.inactive) {
			search_mode = 1;
			search_in_catalogue(this.name_key)
			state = 1;
		}
	}

	preUpdate() {
		if(cursorY + frame_offset_y > DEFAULT_HEIGHT - CATALOGUE_BOTTOM_BAR_HEIGHT) {
			this.inactive = true;
		}else{
			this.inactive = false;
		}
	}

	hover() {
		let p = names_csv.findRow(this.name_key, 0);
		preview_filmmaker(p);
	}
}

class StudioSearchButton extends Interactable {
	constructor(x, y, width, height, image = null, image_scale = 1, caption_chn = "", caption_eng = "", rect_fill = color(255), caption_size = 20) {
		super(x, y, width, height, image, image_scale, caption_chn, caption_eng, rect_fill, caption_size, true);
	}
	
	interact() {
		if(!this.inactive) {
			search_in_studios(search_field.value());
		}
	}
}

class ClearStudioSearchButton extends Interactable {
	constructor(x, y, width, height, image = null, image_scale = 1, caption_chn = "", caption_eng = "", rect_fill = color(255), caption_size = 20) {
		super(x, y, width, height, image, image_scale, caption_chn, caption_eng, rect_fill, caption_size, true);
	}
	
	interact() {
		if(!this.inactive) {
			search_field.value('');
			if(studio_search_input.length > 0)
				search_in_studios('');
		}
	}
}

class StudioSelectButton extends Interactable {
	constructor(x, y, width, height, image = null, image_scale = 1, studio_key = "", caption_chn = "", caption_eng = "", rect_fill = color(255), caption_size = 20) {
		super(x, y, width, height, image, image_scale, caption_chn, caption_eng, rect_fill, caption_size, false);
		this.studio_key = studio_key;
	}
	
	interact() {
		if(!this.inactive) {
			search_mode = 2;
			search_in_catalogue(this.studio_key);
			state = 1;
		}
	}

	preUpdate() {
		if(cursorY + frame_offset_y > DEFAULT_HEIGHT - CATALOGUE_BOTTOM_BAR_HEIGHT) {
			this.inactive = true;
		}else{
			this.inactive = false;
		}
	}

	hover() {
		let s = studios.get(this.studio_key);
		preview_studio(s);
	}
}

class ToCatalogueButton extends Interactable {
	constructor(x, y, width, height, image = null, image_scale = 1, caption_chn = "", caption_eng = "", rect_fill = color(255), caption_size = 20) {
		super(x, y, width, height, image, image_scale, caption_chn, caption_eng, rect_fill, caption_size, true);
	}
	
	interact() {
		if(!this.inactive) {
			show_all_films_in_catalogue();
			state = 1;
		}
	}
}

class ToMapButton extends Interactable {
	constructor(x, y, width, height, image = null, image_scale = 1, caption_chn = "", caption_eng = "", rect_fill = color(255, 250, 200), caption_size = 20) {
		super(x, y, width, height, image, image_scale, caption_chn, caption_eng, rect_fill, caption_size, true);
	}
	
	interact() {
		if(!this.inactive) {
			start_search_map();
			state = 2;
		}
	}
}

class ToStaffButton extends Interactable {
	constructor(x, y, width, height, image = null, image_scale = 1, caption_chn = "", caption_eng = "", rect_fill = color(255, 250, 200), caption_size = 20) {
		super(x, y, width, height, image, image_scale, caption_chn, caption_eng, rect_fill, caption_size, true);
	}
	
	interact() {
		if(!this.inactive) {
			show_all_names_in_name_search();
			state = 3;
		}
	}
}

class ToStudioButton extends Interactable {
	constructor(x, y, width, height, image = null, image_scale = 1, caption_chn = "", caption_eng = "", rect_fill = color(255, 250, 200), caption_size = 20) {
		super(x, y, width, height, image, image_scale, caption_chn, caption_eng, rect_fill, caption_size, true);
	}
	
	interact() {
		if(!this.inactive) {
			show_all_names_in_studio_search();
			state = 4;
		}
	}
}