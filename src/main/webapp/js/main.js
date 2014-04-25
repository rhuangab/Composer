var ROOT = "/theComposerServlet";
var CHORDNUM = 4;
var NOTENUM = 4;
var STAVEWIDTHUNIT = 40;
var STAVEWIDTH = 200;
var STAVEHEIGHT = 150;
var STAVEPADDING = 20;
var STAVETOPPADDING = 0;
var SPEEDUNIT = 0.100;
var SPEEDBASE = 0.500;
var HARDNESS = 100;
var preDefNotes = [[[]]];

function setLoad(message)
{
	var mask = $('<div class="spinner-back" id="loadingMask"><div class="spinner-content"><div class="spinner"><div class="dot1"></div><div class="dot2"></div></div><div class="spinner-message"><span>'+message+'</span></div></div></div>');
	mask.prependTo('body');
	mask.fadeIn(200);
}

function deleteLoad()
{
	$('#loadingMask').fadeOut(1000);
	setTimeout(function(){$('#loadingMask').remove();},1000);
}


function pitchValueToVexKey(pitchValue){
	var key = MIDI.noteToKey[pitchValue];
	var level = key[key.length-1]-1+2;
	jjj = key;
	key = key.substring(0,key.length-1) + '/' + level;
	return key;
}

function vexKeyToPitchValue(key)
{
	key = key.replace('/','');
	var pitchValue = MIDI.keyToNote['A0'];
}

function copyVexNote(oldNote,newPitchValue)
{
	var duration = oldNote.duration;
	var hasDot = oldNote.hasDot;
	
}

function newAnnotation(text) {
	return (
					new Vex.Flow.Annotation(text)).
	setFont("Times", 10).
	setVerticalJustification(Vex.Flow.Annotation.VerticalJustify.BOTTOM);
}

function createVexNote(note_struct)
{
	var dur = {
		'2':[2,0,32],
		'4':[4,0,16],
		'6':[4,1,16],
		'8':[8,0,8],
		'12':[8,1,8],
		'16':[16,0,4],
		'24':[24,1,4],
		'32':[32,0,2],
		'48':[32,1,2],
		'64':[64,0,1]
		};
	var playLength = note_struct.duration;
	var key = note_struct.keys;
	//if(key == -1) key = 60;
	if(key == -1)
	{
		var key = pitchValueToVexKey(myViewModel.selectedScale()*12);
		var hasDot = dur[note_struct.duration][1] == 1?true:false;
		var newDur = dur[note_struct.duration][2];
		var singleNote = new Vex.Flow.StaveNote({duration:""+newDur+"r",keys:[key]});
		//console.log(""+newDur+"r");
		singleNote.playLength = playLength;
		singleNote.isRestNote = true;
		singleNote.hasDot = hasDot;
		if(hasDot) singleNote.addDotToAll();
		return singleNote;
	}
	else{
		key = pitchValueToVexKey(key);
		var newDur = dur[note_struct.duration][2];
		var hasDot = dur[note_struct.duration][1] == 1?true:false;
		var singleNote = new Vex.Flow.StaveNote({duration:""+newDur,keys:[key]});
		singleNote.playLength = playLength;
		if(key.length == 4) singleNote.addAccidental(0, new Vex.Flow.Accidental("b"));
		singleNote.hasDot = hasDot;
		if(hasDot) singleNote.addDotToAll();
		if(note_struct.keys > 70)
			singleNote.setStemDirection(-1);
		singleNote.isRestNote = false;
		if(note_struct.lrcText){
			console.log(note_struct.lrcText);
			singleNote.addAnnotation(0, newAnnotation(note_struct.lrcText));
		}
		return singleNote;
	}
}

function drawEmptyStaves()
{
	var canvas = $("canvas")[0];
	var renderer = new Vex.Flow.Renderer(canvas,
																	 Vex.Flow.Renderer.Backends.CANVAS);
	var ctx = renderer.getContext();
	var padding = STAVEPADDING;
	var x = 0;
	var y = 0;
	for(var i=0;i<2;i++)
	{
		for(var j=0;j<4;j++)
		{
			x = STAVEWIDTH*j;
			y = STAVEHEIGHT*i;
			var staveBar = new Vex.Flow.Stave(padding + x, y, STAVEWIDTH);
			if (x == 0) {
				staveBar.addClef("treble");
				//width = width+20;
			}
			else
			{
				x +=20; //space for clef.
			}
			staveBar.setContext(ctx).draw();
		}
	}
}

(function ($) {
	ViewModel = function(){
 	var self = this;
	self.octaveArray = ko.observableArray([1,2,3,4,5,6,7]);
	self.octave = ko.observable(1);
	self.playerState = ko.observable("stop");
	self.playerState.extend({notify: 'always'});
	self.selectedBeats = 4;
	self.selectedBeatType = 4;
	self.selectedMajor = 0;
	self.selectedScale = ko.observable(5);
	self.playSpeed = ko.observable(0);
	$(".player").click(function(event){
		targetButton = $(event.target).closest('.player');
		targetButton.siblings().removeClass('active');
		targetButton.focus();
		self.playerState(targetButton.attr('value'));
	});
	$(".beats").click(function(event){
		targetButton = $(event.target).closest('.beats');
		targetButton.siblings().removeClass('active');
		targetButton.focus();
		self.selectedBeats = targetButton.attr('beats');
		self.selectedBeatType = targetButton.attr('beats-type');
		STAVEWIDTH = self.selectedBeats * STAVEWIDTHUNIT;
	});
	$(".major").click(function(event){
		targetButton = $(event.target).closest('.major');
		targetButton.siblings().removeClass('active');
		targetButton.focus();
		self.selectedMajor = targetButton.attr('value');
	});
	$(".instrument").click(function(event){
		targetButton = $(event.target).closest('.instrument');
		targetButton.siblings().removeClass('active');
		targetButton.focus();
		MIDI.programChange(0, targetButton.attr('value'));
		/*if(targetButton.attr('value') == 40 ||targetButton.attr('value')==66) SPEEDBASE = ;
		else SPEEDBASE = 0.25;*/
	});
	$(".scale").click(function(event){
		targetButton = $(event.target).closest('.scale');
		targetButton.siblings().removeClass('active');
		//targetButton.focus();
		var temp = self.selectedScale();
		self.selectedScale(temp + parseInt(targetButton.attr('value')));
		if(self.selectedScale() < 2) self.selectedScale(self.selectedScale()+1);
		else if(self.selectedScale() > 8) self.selectedScale(self.selectedScale()-1);
		if(self.selectedScale() >= 6){
			STAVETOPPADDING = [20,35,105][self.selectedScale()%6];
		}
		else{
			STAVETOPPADDING = 0;
		}
	});
	$(".speed").click(function(event){
		targetButton = $(event.target).closest('.speed');
		targetButton.siblings().removeClass('active');
		//targetButton.focus();
		var temp = self.playSpeed();
		var add = parseInt(targetButton.attr('value'));
		var temp = temp + add;
		if(temp < -5) self.playSpeed(-5);
		else if(temp > 5) self.playSpeed(5);
		else self.playSpeed(temp);
		if(add > 0){
			SPEEDBASE -= SPEEDUNIT;
		}
		else{
			SPEEDBASE += SPEEDUNIT;
		}
	});
	MIDI.loadPlugin({
			soundfontUrl: "./soundfont/",
			instruments: ["acoustic_grand_piano","glockenspiel","acoustic_guitar_nylon","violin","tenor_sax","tinkle_bell"],
			//instrument: "acoustic_guitar_nylon",
			callback: function() {
				//$("#loadingMask").fadeOut(1000);
				deleteLoad();
				/*self.playerNote = function(note){
					var delay = 0; // play one note every quarter second
					var note = noteArray[1]+12*(self.octave()-1); // the MIDI note
					
					var velocity = 127; // how hard the note hits
					// play the note
					MIDI.setVolume(0, 127);
					MIDI.noteOn(0, note, velocity, delay);
					MIDI.noteOff(0, note, delay + 0.75);
					delay+=1;
					MIDI.noteOn(0, note, velocity, delay);
					};*/
			//MIDI.noteOn(0, 60, 127, 0);
			}
		});
	self.MIDI = MIDI;
	self.drawCanvas = function(){
		$("canvas")[0].getContext('2d').clearRect(0,0,$("canvas").attr('width'),$("canvas").attr('height'));
		if(self.canvas){
			self.canvas.unsubscribe();
 		}
		self.canvas = new ViewModel.Canvas();
	
	};
	self.playerTimer = null;
	self.playNextNote = function(){
		self.canvas.colorNextNote();
		var curNote = self.canvas.curPlayInfo.curPlayNotes();
		var speed = SPEEDBASE * curNote.playLength/32 * 1000;
		//var keyValue = curNote.keys[0];
		var noteValue = curNote.keyProps[0].int_value;
		if(!self.canvas.curPlayInfo.isLastNote()){
			if(!curNote.isRestNote){
				self.MIDI.noteOn(0,noteValue,HARDNESS,0);
			}
			self.playerTimer = setTimeout(self.playNextNote,speed);
		}
		else
		{
			if(!curNote.isRestNote){
				self.MIDI.noteOn(0,noteValue,HARDNESS,0);
			}
			self.canvas.colorNextNote();
			//self.canvas.curPlayInfo.setDefaultValue();
			setTimeout(function(){$(".player[value='stop']").click()},speed);
		}
		if(self.canvas.curPlayInfo.curPlayCol == 0 && self.canvas.curPlayInfo.curPlayIndex == 0)
		{
			$('.middle-frame').animate({scrollTop:curNote.ys[0]-100}, '500');
		}
 	}
	self.playerState.subscribe(function(newValue) {
		var canvas = self.canvas;
		if(newValue =="play" && !self.playerTimer)
		{
			self.playerTimer = setTimeout(self.playNextNote,0);
		}
		else if(newValue == 'pause')
		{
			clearTimeout(self.playerTimer);
			self.playerTimer = null;
		}
		else if(newValue == 'stop')
		{
			clearTimeout(self.playerTimer);
			self.playerTimer = null;
			self.canvas.curPlayInfo.setDefaultValue();
		}
	});
}
	
ViewModel.Canvas = function()
{
	//$("canvas")[0].getContext('2d').clearRect(131,50,100,100)
	var self = this;
	self.notes = preDefNotes;
	self.staveWidth = STAVEWIDTH;
	self.staveHeight = STAVEHEIGHT;
	$("canvas").attr('height',self.staveHeight*preDefNotes.length);
	self.canvas = $("canvas")[0];
	self.renderer = new Vex.Flow.Renderer(self.canvas,
		Vex.Flow.Renderer.Backends.CANVAS);
	self.curRow = ko.observable(0);
	self.curCol = ko.observable(0);
	self.curIndex = ko.observable(0);
	self.currentSelected = ko.observable(null);
	self.ctx = self.renderer.getContext();
	self.curX = 0;
	self.y = 0;
	self.padding = STAVEPADDING;
	self.subscriptionCollection = [];
	self.staves = [];
	self.drawTempoStaveBar = function(x,y,width, tempo, tempo_y, notes) {
		if(y == 0) y = STAVETOPPADDING+y;
    var staveBar = new Vex.Flow.Stave(self.padding + x, y, width);
    if (x == 0) {
 			staveBar.addClef("treble");
			width = width+20;
		}
		else
		{
 			x +=20; //space for clef.
		}
    staveBar.setTempo(tempo, tempo_y);
    staveBar.setContext(self.ctx).draw();

    var notesBar = notes;
		var consecutiveSmallNotes = 0;
		var beams = [];
		for(var i=0;i<notes.length;i++)
		{
 			if(notes[i].playLength < 16){
				consecutiveSmallNotes++;
			}
			else{
				if(consecutiveSmallNotes >= 2){
					console.log(notes.slice(i-consecutiveSmallNotes,i));
 					beams.push(new Vex.Flow.Beam(notes.slice(i-consecutiveSmallNotes,i)));
 				}
				consecutiveSmallNotes = 0;
			}
 		}
    Vex.Flow.Formatter.FormatAndDraw(self.ctx, staveBar, notesBar);
		for(var i=0;i<beams.length;i++){
			beams[i].setContext(self.ctx).draw();
		}
		self.staves.push(staveBar);
  };
	
	self.unsubscribe = function(){
 		$.each(self.subscriptionCollection,function(key,value){value.dispose();});
		$('body').off('keydown');
 	}
	
	//self.drawTempoStaveBar(120, { duration: "q", dots: 1, bpm: 80 }, 0,self.notes[0]);
	self.setContextColor = function(color){
		self.ctx.setFillStyle(color);
		self.ctx.setStrokeStyle(color);
		self.ctx.setShadowColor(color);
	}
	self.setContextColor('black');
	self.draw = function(){
		for(var i=0;i<self.notes.length;i++)
		{
			for(var j=0;j<self.notes[i].length;j++)
			{
 				self.drawTempoStaveBar(self.staveWidth*j,(STAVEHEIGHT+STAVETOPPADDING)*i,self.staveWidth, {}, 0,self.notes[i][j]);
			}
		}
 	}
	self.draw();
	self.redraw = function(){
 		self.canvas.getContext('2d').clearRect(0,0,$(self.canvas).attr('width'),$(self.canvas).attr('height'));
		self.setContextColor('black');
		self.draw();
		self.setContextColor('red');
 	}
	self.changePitchValue = function(increase){
		var cmajorp = [2,0,2,0,1,2,0,2,0,2,0,1];
		var cmajord = [1,0,2,0,2,1,0,2,0,2,0,2];
 		var i = self.curRow();
		var j = self.curCol();
		var pitchValue = self.currentSelected().keyProps[0].int_value;
		if(increase){
			if(pitchValue == 108) return;
			pitchValue += 1;//cmajorp[pitchValue%12];
		}
		else{
			if(pitchValue == 21) return;
			pitchValue -= 1;//cmajord[pitchValue%12];
		}
		if(self.currentSelected().isRestNote) pitchValue = -1;
		var playLength = self.notes[i][j][self.curIndex()].playLength;
		var singleNote = createVexNote({ keys: pitchValue, duration: playLength });
		//var singleNote = copyVexNote(self.currentSelected(),pitchValue);
		//var singleNote = new Vex.Flow.StaveNote({ keys: [newKey], duration: oldDuration });
		self.notes[i][j][self.curIndex()] = singleNote;
		self.currentSelected(null);
		var width = self.staveWidth;
		var x = self.staveWidth*j;
		var y = self.staveHeight*i;
		var clearWidth = width;
		var clearX = x + self.padding+1;
//		self.canvas.getContext('2d').clearRect(clearX,y,clearWidth-1,self.staveHeight);
		self.redraw();
//		self.setContextColor('black');
//		self.drawTempoStaveBar(x,y,width, {}, 0,self.notes[i][j]);
//		self.setContextColor('red');
		self.currentSelected(self.notes[i][j][self.curIndex()]);
		self.currentSelected.extend({notify: 'always'});
 	}
	
	$("body").keydown(function(event){
		//console.log(self.currentSelected());
		var row = self.curRow();
		var col = self.curCol();
		var index = self.curIndex();
		if(event.keyCode == 39)//right
		{
			if(index == self.notes[row][col].length-1)
			{
				if(col == self.notes[row].length-1) return;
				else{ ++col;self.curCol(col);}
				index = 0;
				self.curIndex(index);
			}
			else{++index;self.curIndex(index);}
			self.currentSelected(self.notes[self.curRow()][self.curCol()][self.curIndex()]);
		}
		else if(event.keyCode == 37)//left
		{
			if(index == 0)
			{
				if(col == 0) return;
				else{ --col;self.curCol(col);}
				index = self.notes[row][col].length-1;
				self.curIndex(index);
			}
			else{--index;self.curIndex(index);}
			self.currentSelected(self.notes[self.curRow()][self.curCol()][self.curIndex()]);
		}
		else if(event.keyCode == 38)//up
		{
			if(row == 0) return;
			else{--row;self.curRow(row);}
			self.currentSelected(self.notes[self.curRow()][self.curCol()][self.curIndex()]);
		}
		else if(event.keyCode == 40)//down
		{
			if(row == self.notes.length-1) return;
			else{++row;self.curRow(row);}
			self.currentSelected(self.notes[self.curRow()][self.curCol()][self.curIndex()]);
		}
		else if(event.keyCode == 219)//increase half pitch
		{
			self.changePitchValue(false);
		}
		else if(event.keyCode == 221)//decrease half pitch
		{
			self.changePitchValue(true);
		}
		});
	self.subscriptionCollection.push(
		self.currentSelected.subscribe(function(oldValue){
			self.setContextColor('black');
			if(oldValue) oldValue.draw();
		},null,'beforeChange')
	);
	self.subscriptionCollection.push(
		self.currentSelected.subscribe(function(newValue){
			self.setContextColor('red');
			if(newValue) newValue.draw();
		})
	);
	self.currentSelected(self.notes[0][0][0]);
	self.setContextColor('red');
	self.currentSelected().draw();
	
	self.curPlayInfo = {
		curPlayRow : -1,
		curPlayCol : -1,
		curPlayIndex : -1,
		curPlayNotes : ko.observable(null)
	};
 self.curPlayInfo.isLastNote = function(){
	 if(self.curPlayInfo.curPlayRow == self.notes.length-1
			&& self.curPlayInfo.curPlayCol == self.notes[self.curPlayInfo.curPlayRow].length-1
			&& self.curPlayInfo.curPlayIndex == self.notes[self.curPlayInfo.curPlayRow][self.curPlayInfo.curPlayCol].length-1)
	 	return true;
	 else return false;
 };
 self.curPlayInfo.setDefaultValue = function(){
 	 self.curPlayInfo.curPlayRow = -1;
	 self.curPlayInfo.curPlayCol = -1;
	 self.curPlayInfo.curPlayIndex = -1;
	 self.curPlayInfo.curPlayNotes(null);
 }
	//self.curPlayNotes = ko.observable(null);
	self.curPlayInfo.curPlayNotes.extend({notify: 'always'});
	self.curPlayInfo.curPlayNotes.subscribe(function(oldValue){
		self.setContextColor('black');
		if(oldValue == self.currentSelected()){self.setContextColor('red');}
		else self.setContextColor('black');
		if(oldValue) oldValue.draw();
	},null,'beforeChange');
	self.curPlayInfo.curPlayNotes.subscribe(function(newValue){
		self.setContextColor('yellow');
		if(newValue) newValue.draw();
	});
	self.colorNextNote = function(){
		if(self.curPlayInfo.curPlayIndex==-1 && self.curPlayInfo.curPlayRow==-1 && self.curPlayInfo.curPlayCol==-1)
		{
			++self.curPlayInfo.curPlayIndex;
			++self.curPlayInfo.curPlayCol;
			++self.curPlayInfo.curPlayRow;
		}
		else if(self.curPlayInfo.curPlayIndex == self.notes[self.curPlayInfo.curPlayRow][self.curPlayInfo.curPlayCol].length-1){
 			if(self.curPlayInfo.curPlayCol == self.notes[self.curPlayInfo.curPlayRow].length-1)
			{
 				if(self.curPlayInfo.curPlayRow == self.notes.length-1)
				{
					setTimeout(self.curPlayInfo.setDefaultValue,SPEEDBASE*1000);
					return;
				}
				else{++self.curPlayInfo.curPlayRow;self.curPlayInfo.curPlayCol=0;self.curPlayInfo.curPlayIndex=0;}
 			}
			else{
 				++self.curPlayInfo.curPlayCol;
				self.curPlayInfo.curPlayIndex = 0;
 			}
		}
		else{
			++self.curPlayInfo.curPlayIndex;
		}
		self.curPlayInfo.curPlayNotes(self.notes[self.curPlayInfo.curPlayRow][self.curPlayInfo.curPlayCol][self.curPlayInfo.curPlayIndex]);
	}
	
}
}(jQuery));


$(document).ready(function($) {
	//setLoad('Loading MIDI sound');
	myViewModel = new ViewModel();
	ko.applyBindings(myViewModel);
	drawEmptyStaves();
	jsonResult = {};
	$('<input>').attr({type: 'hidden',name:'major',value:myViewModel.selectedMajor}).appendTo('form');
	$('<input>').attr({type: 'hidden',name:'beats',value:myViewModel.selectedBeats}).appendTo('form');
	$('<input>').attr({type: 'hidden',name:'beatType',value:myViewModel.selectedBeatType}).appendTo('form');
	$('<input>').attr({type: 'hidden',name:'scale',value:myViewModel.selectedScale()}).appendTo('form');
	$("#create_button").click(function(){
		setLoad('Generateing ...');
		$('input[name=major]').attr('value',myViewModel.selectedMajor);
		$('input[name=beats]').attr('value',myViewModel.selectedBeats);
		$('input[name=beatType]').attr('value',myViewModel.selectedBeatType);
		$('input[name=scale]').attr('value',myViewModel.selectedScale());
		$.ajax({
			url: ROOT,
			type: 'POST',
			data: $('form').serialize(),
			contentType:'application/x-www-form-urlencoded; charset=UTF-8',
			complete: deleteLoad,
			success: function(result){
				/*try{
						result = JSON.parse(result);
				} catch(err) {console.log("Parse Failed");}*/
				jsonResult = result;
				preDefNotes = [];
				var staveIndex = 0;
				var chordIndex = 0;
				var total = 32;
				var cur = 0;
				//result = {"notes":[{"duration":"4","keys":83},{"duration":"4","keys":83},{"duration":"4","keys":80},{"duration":"4","keys":78},{"duration":"4","keys":78},{"duration":"4","keys":78},{"duration":"4","keys":76},{"duration":"4","keys":74},{"duration":"4","keys":76},{"duration":"4","keys":76},{"duration":"4","keys":73},{"duration":"4","keys":71},{"duration":"4","keys":71},{"duration":"4","keys":69},{"duration":"4","keys":68},{"duration":"4","keys":66},{"duration":"4","keys":68},{"duration":"4","keys":68},{"duration":"4","keys":69}]}
				for(var i=0; i<result.notes.length;i++){
					//console.log(parseInt(i/CHORDNUM)+" "+i%CHORDNUM);
					if(i%CHORDNUM==0){
						preDefNotes.push([[]]);
					}
					else{
					 	preDefNotes[parseInt(i/CHORDNUM)].push([]);
					}
					
					for(var j=0; j<result.notes[i].length;j++){
						//result.notes[i].duration = "4";
	//					cur += 32/result.notes[i].duration;
	//					if(cur > total && i > 0)
	//					{
	//						++chordIndex;
	//						if(chordIndex%CHORDNUM == 0)
	//						{
	//							++staveIndex;
	//							preDefNotes.push([[]]);
	//							chordIndex = 0;
	//						}
	//						else{
	//							preDefNotes[staveIndex].push([]);
	//						}
	//						cur = 32/result.notes[i].duration;
	//					}
						//result.notes[i].keys = [noteToVexKey(result.notes[i].keys)];
						//result.notes[i].keys = i%2==0?['Cb/4']:['c/4'];
						
						preDefNotes[parseInt(i/CHORDNUM)][i%CHORDNUM].push(createVexNote(result.notes[i][j]));
					}
				}
				
				myViewModel.drawCanvas();
			}
		});
	});
	$("input[name=lrc]").val("I'm at a payphone, trying to call home. All of my change I spent on you. Where have the times gone");
	$("#create_button").click();
		
});