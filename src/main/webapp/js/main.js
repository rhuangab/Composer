var ROOT = "/theComposerServlet";
var CHORDNUM = 4;
var NOTENUM = 4;
var STAVEWIDTHUNIT = 40;
var STAVEWIDTH = 160;
var STAVEHEIGHT = 150;
var STAVEPADDING = 20;
var SPEEDUNIT = 0.200;
var SPEEDBASE = SPEEDUNIT;
var HARDNESS = 100;
var preDefNotes = [[[]]];
/*var preDefNotes = [[
		[new Vex.Flow.StaveNote({ keys: ["c/4"], duration: "4" }),
      new Vex.Flow.StaveNote({ keys: ["D/4"], duration: "4" }),
      new Vex.Flow.StaveNote({ keys: ["b/4"], duration: "4" }),
      new Vex.Flow.StaveNote({ keys: ["c/4"], duration: "4" })
			],
		[new Vex.Flow.StaveNote({ keys: ["c/4"], duration: "4" }),
      new Vex.Flow.StaveNote({ keys: ["d/4"], duration: "4" }),
      new Vex.Flow.StaveNote({ keys: ["b/4"], duration: "4" }),
      new Vex.Flow.StaveNote({ keys: ["c/4"], duration: "4" })
		],
		[new Vex.Flow.StaveNote({ keys: ["c/4"], duration: "4" }),
      new Vex.Flow.StaveNote({ keys: ["d/4"], duration: "2" }),
      new Vex.Flow.StaveNote({ keys: ["e/4"], duration: "4" })
		],
		[new Vex.Flow.StaveNote({ keys: ["c/4"], duration: "4" }),
      new Vex.Flow.StaveNote({ keys: ["d/4"], duration: "4" }),
      new Vex.Flow.StaveNote({ keys: ["b/4"], duration: "4" }),
      new Vex.Flow.StaveNote({ keys: ["e/4"], duration: "4" })
			]],
		[[new Vex.Flow.StaveNote({ keys: ["c/4"], duration: "4" }),
      new Vex.Flow.StaveNote({ keys: ["e/4"], duration: "4" }),
      new Vex.Flow.StaveNote({ keys: ["b/4"], duration: "4" }),
      new Vex.Flow.StaveNote({ keys: ["c/4"], duration: "4" })
			],
		[new Vex.Flow.StaveNote({ keys: ["c/4"], duration: "4" }),
      new Vex.Flow.StaveNote({ keys: ["d/4"], duration: "4" }),
      new Vex.Flow.StaveNote({ keys: ["b/4"], duration: "4" }),
      new Vex.Flow.StaveNote({ keys: ["c/4"], duration: "4" })
		],
		[new Vex.Flow.StaveNote({ keys: ["c/4"], duration: "4" }),
      new Vex.Flow.StaveNote({ keys: ["d/4"], duration: "2" }),
      new Vex.Flow.StaveNote({ keys: ["c/4"], duration: "4" })
		],
		[new Vex.Flow.StaveNote({ keys: ["c/4"], duration: "4" }),
      new Vex.Flow.StaveNote({ keys: ["d/4"], duration: "4" }),
      new Vex.Flow.StaveNote({ keys: ["b/4"], duration: "4" }),
      new Vex.Flow.StaveNote({ keys: ["c/4"], duration: "4" })
			]],
		[[new Vex.Flow.StaveNote({ keys: ["c/4"], duration: "4" }),
      new Vex.Flow.StaveNote({ keys: ["e/4"], duration: "4" }),
      new Vex.Flow.StaveNote({ keys: ["b/4"], duration: "4" }),
      new Vex.Flow.StaveNote({ keys: ["c/4"], duration: "4" })
			],
		[new Vex.Flow.StaveNote({ keys: ["c/4"], duration: "4" }),
      new Vex.Flow.StaveNote({ keys: ["d/4"], duration: "4" }),
      new Vex.Flow.StaveNote({ keys: ["b/4"], duration: "4" }),
      new Vex.Flow.StaveNote({ keys: ["c/4"], duration: "4" })
		],
		[new Vex.Flow.StaveNote({ keys: ["c/4"], duration: "4" }),
      new Vex.Flow.StaveNote({ keys: ["d/4"], duration: "2" }),
      new Vex.Flow.StaveNote({ keys: ["c/4"], duration: "4" })
		],
		[new Vex.Flow.StaveNote({ keys: ["c/4"], duration: "4" }),
      new Vex.Flow.StaveNote({ keys: ["d/4"], duration: "4" }),
      new Vex.Flow.StaveNote({ keys: ["b/4"], duration: "4" }),
      new Vex.Flow.StaveNote({ keys: ["c/4"], duration: "4" })
			]]
			];
*/

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
	var key = pitchValueToVexKey(note_struct.keys);
	var newDur = dur[note_struct.duration][2];
	var hasDot = dur[note_struct.duration][1] == 1?true:false;
	var singleNote = new Vex.Flow.StaveNote({duration:""+newDur,keys:[key]});
	if(key.length == 4) singleNote.addAccidental(0, new Vex.Flow.Accidental("b"));
	if(hasDot) singleNote.addDotToAll();
	if(note_struct.keys > 70)
		singleNote.setStemDirection(-1);
	return singleNote;
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
	self.selectedScale = ko.observable(4);
	self.playSpeed = ko.observable(1);
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
		if(targetButton.attr('value') == 40 ||targetButton.attr('value')==66) SPEEDBASE = 0.5;
		else SPEEDBASE = 0.25;
	});
	$(".scale").click(function(event){
		targetButton = $(event.target).closest('.scale');
		targetButton.siblings().removeClass('active');
		//targetButton.focus();
		var temp = self.selectedScale();
		self.selectedScale(temp + parseInt(targetButton.attr('value')));
		if(self.selectedScale() < 2) self.selectedScale(self.selectedScale()+1);
		else if(self.selectedScale() > 8) self.selectedScale(self.selectedScale()-1);
	});
	$(".speed").click(function(event){
		targetButton = $(event.target).closest('.speed');
		targetButton.siblings().removeClass('active');
		//targetButton.focus();
		var temp = self.playSpeed();
		var temp = temp + parseInt(targetButton.attr('value'));
		if(temp < 0) self.playSpeed(1);
		else if(temp > 5) self.playSpeed(5);
		else self.playSpeed(temp);
		SPEEDBASE = SPEEDUNIT / self.playSpeed();
	});
	MIDI.loadPlugin({
			soundfontUrl: "./soundfont/",
			instruments: ["acoustic_grand_piano","glockenspiel","acoustic_guitar_nylon","violin","tenor_sax","tinkle_bell"],
			//instrument: "acoustic_guitar_nylon",
			callback: function() {
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
		var speed = SPEEDBASE * (4 / curNote.duration)*1000;
		//var keyValue = curNote.keys[0];
		var noteValue = curNote.keyProps[0].int_value;
		if(!self.canvas.curPlayInfo.isLastNote()){
			self.MIDI.noteOn(0,noteValue,HARDNESS,0);
			self.playerTimer = setTimeout(self.playNextNote,speed);
		}
		else
		{
			self.MIDI.noteOn(0,noteValue,HARDNESS,0);
			self.canvas.colorNextNote();
			//self.canvas.curPlayInfo.setDefaultValue();
			setTimeout(function(){$(".player[value='stop']").click()},speed);
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

    Vex.Flow.Formatter.FormatAndDraw(self.ctx, staveBar, notesBar);
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
 				self.drawTempoStaveBar(self.staveWidth*j,self.staveHeight*i,self.staveWidth, {}, 0,self.notes[i][j]);
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
		var oldDuration = self.notes[i][j][self.curIndex()].duration;
		var singleNote = createVexNote({duration:oldDuration,keys:pitchValue});
		//var singleNote = new Vex.Flow.StaveNote({ keys: [newKey], duration: oldDuration });
		self.notes[i][j][self.curIndex()] = singleNote;
		self.currentSelected(null);
		var width = self.staveWidth;
		var x = self.staveWidth*j;
		var y = self.staveHeight*i;
		var clearWidth = width;
		var clearX = x + self.padding+1;
		console.log(clearX);
		console.log(clearWidth);
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
	ko.bindingHandlers.bsChecked = {/*
    init: function (element, valueAccessor, allBindingsAccessor,
    viewModel, bindingContext) {
				console.log(element);
        var value = valueAccessor();
        var newValueAccessor = function () {
            return {
                change: function () {
                    value(element.value);
                }
            }
        };
        ko.bindingHandlers.event.init(element, newValueAccessor,
        allBindingsAccessor, viewModel, bindingContext);
    },
    update: function (element, valueAccessor, allBindingsAccessor,viewModel, bindingContext) {
				console.log(ko.utils.unwrapObservable(valueAccessor()));
        if ($(element).val() == ko.unwrap(valueAccessor())) {
            $(element).closest('.btn').button('toggle');
        }
    }*/
	};
	
	myViewModel = new ViewModel();
	ko.applyBindings(myViewModel);
	drawEmptyStaves();
	jsonResult = {};
	$('<input>').attr({type: 'hidden',name:'major',value:myViewModel.selectedMajor}).appendTo('form');
	$('<input>').attr({type: 'hidden',name:'beats',value:myViewModel.selectedBeats}).appendTo('form');
	$('<input>').attr({type: 'hidden',name:'beatType',value:myViewModel.selectedBeatType}).appendTo('form');
	$('<input>').attr({type: 'hidden',name:'scale',value:myViewModel.selectedScale()}).appendTo('form');
	$("#create_button").click(function(){
		$('input[name=major]').attr('value',myViewModel.selectedMajor);
		$('input[name=beats]').attr('value',myViewModel.selectedBeats);
		$('input[name=beatType]').attr('value',myViewModel.selectedBeatType);
		$('input[name=scale]').attr('value',myViewModel.selectedScale());
		$.ajax({
			url: ROOT,
			type: 'POST',
			data: $('form').serialize(),
			contentType:'application/x-www-form-urlencoded; charset=UTF-8',
			success: function(result){
				/*try{
						result = JSON.parse(result);
				} catch(err) {console.log("Parse Failed");}*/
				jsonResult = result;
				preDefNotes = [[[]]];
				var staveIndex = 0;
				var chordIndex = 0;
				var total = 32;
				var cur = 0;
				//result = {"notes":[{"duration":"4","keys":83},{"duration":"4","keys":83},{"duration":"4","keys":80},{"duration":"4","keys":78},{"duration":"4","keys":78},{"duration":"4","keys":78},{"duration":"4","keys":76},{"duration":"4","keys":74},{"duration":"4","keys":76},{"duration":"4","keys":76},{"duration":"4","keys":73},{"duration":"4","keys":71},{"duration":"4","keys":71},{"duration":"4","keys":69},{"duration":"4","keys":68},{"duration":"4","keys":66},{"duration":"4","keys":68},{"duration":"4","keys":68},{"duration":"4","keys":69}]}
				for(var i=0; i<result.notes.length;i++){
					console.log(parseInt(i/CHORDNUM)+" "+i%CHORDNUM);
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
		
});