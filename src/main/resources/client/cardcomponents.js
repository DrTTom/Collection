

class CardMaker extends DefaultTile {
	createContent(refId) {
		super.createContent(refId);
		$('div', this).classList.add('c-medium');
	}

	fillContent(node, data) {
		BuildNode.div().in(node).text(data.attributes.place);
		//buildChildNode(node, 'div').get().innerHTML = data.attributes.place;
		buildChildNode(node, 'div').get().innerHTML = data.attributes.from + ' - ' + data.attributes.to;
		buildChildNode(node, 'p').class('scroll5lines').get().innerHTML = data.attributes.remark;
	}
}


class DeckSmall extends DefaultTile {
	createContent(refId) {
		super.createContent(refId);
		$('div', this).classList.add('c-small');
	}
	fillContent(node, data) {
		buildChildNode(node, 'div').get().innerHTML = i18n('maker', data.attributes.maker);
		buildChildNode(node, 'div').get().innerHTML = DeckSmall.timeString(data.attributes);
		let text = data.attributes.remark ? data.attributes.remark : i18n('pattern', data.attributes.pattern);
		buildChildNode(node, 'p').class('scroll2lines').get().innerHTML = text;
	}

	static timeString(attrs) {
		let to = attrs.printedLatest;
		let from = attrs.printedEarliest;
		if (to) {
			return to == from ? to : (from ? 'zwischen ' + from + ' und ' : 'spätestens ') + to;
		}
		return from ? 'frühestens ' + from : 'keine Zeitangabe';
	}

}

class DeckMedium extends DefaultTile {
	createContent(refId) {
		super.createContent(refId);
		$('div', this).classList.add('c-medium');
	}
	fillContent(node, data) {
		buildChildNode(node, 'img').class('image-small').attribute('src', '/download/' + data.attributes.image);
		buildChildNode(node, 'div').text(i18n('maker', data.attributes.maker));
		buildChildNode(node, 'div').text(DeckSmall.timeString(data.attributes));
		buildChildNode(node, 'div').class('separated').text(data.attributes.numberCards + ' Blatt, ' + data.attributes.format);
		let text = data.attributes.remark ? data.attributes.remark : i18n('pattern', data.attributes.pattern);
		buildChildNode(node, 'p').class('scroll3lines separated clear').text(text);
	}
}

class DeckBig extends DefaultTile {
	createContent(refId) {
		super.createContent(refId);
		$('div', this).classList.add('c-big');
	}
	fillContent(node, data) {
		buildChildNode(node, 'img').class('image-default').attribute('src', '/download/' + data.attributes.image);
		buildChildNode(node, 'div').text(i18n('maker', data.attributes.maker))
		buildChildNode(node, 'div').text(DeckSmall.timeString(data.attributes));
		buildChildNode(node, 'div').class('separated').text(data.attributes.numberCards + ' Blatt, ' + data.attributes.format);
		let text = 'erworben '+data.attributes.bought+', '+i18n('condition', data.attributes.condition);
		buildChildNode(node, 'div').class('separated').text(text);
		text = data.attributes.remark ? data.attributes.remark : i18n('pattern', data.attributes.pattern);
		buildChildNode(node, 'p').class('scroll3lines separated clear').text(text);		
	}
}

class FullDeck extends FullView {
	fillContent(node, data) {
		node.classList.add('dialog-big')
		let values = data.attributes;
		buildChildNode(node, 'img').attribute("class", "image-big").attribute('src', '/download/' + values.image);
		buildChildNode(node, 'div').text(i18n('maker', values.maker))
		buildChildNode(node, 'div').text(DeckSmall.timeString(values));
		buildChildNode(node, 'div').class('separated').text(values.numberCards + ' Blatt, ' + values.format);
		buildChildNode(node, 'div').text('Auge des Königs bei '+values.specialMeasure);
		buildChildNode(node, 'div').class('separated')
		      .text('Farbzeichen '+i18n('suits',values.suits)+', '+i18n('pattern', values.pattern));
		buildChildNode(node, 'div').text('Index ' + values.index + ' x '+ values.numIndex);
		buildChildNode(node, 'div').class('separated').text('Kennzeichen: '+ i18n('makerSign', values.makerSign)+ ', ' +i18n('taxStamp', values.taxStamp));
		buildChildNode(node, 'div').class('separated').text('Nummer vom Hersteller: '+  values.refMaker);
		buildChildNode(node, 'div').text('siehe '+  values.refCat);
		
		buildChildNode(node, 'p').class('scroll3lines separated clear').text(values.remark);
		buildChildNode(node, 'div').class('separated').text(
		'erworben ' + values.bought + ', ' + i18n('condition', values.condition) + ', befindet sich in ' + values.location);
	}
}

class MakerSign extends DefaultTile {
	createContent(refId) {
		super.createContent(refId);
		$('div', this).classList.add('c-small');
	}
	fillContent(node, data) {
		buildChildNode(node, 'img').class('image-tiny').attribute('src', '/download/' + data.attributes.image);
		buildChildNode(node, 'div').text(i18n('maker', data.attributes.maker))
		buildChildNode(node, 'div').class('clear separated').text(data.attributes.usedFrom +' bis '+ data.attributes.usedTo);			
	}
}

class TaxStamp extends DefaultTile {
	createContent(refId) {
		super.createContent(refId);
		$('div', this).classList.add('c-medium');
	}
	fillContent(node, data) {
		buildChildNode(node, 'img').class('image-small').attribute('src', '/download/' + data.attributes.image);
		buildChildNode(node, 'div').class('clear separated').text(data.attributes.usedFrom +' bis '+ data.attributes.usedTo);			
	}
}

class Pattern extends DefaultTile {
	createContent(refId) {
		super.createContent(refId);
		$('div', this).classList.add('c-medium');
	}
	fillContent(node, data) {
		buildChildNode(node, 'img').class('image-wide').attribute('src', '/download/' + data.attributes.image);
		buildChildNode(node, 'div').class('clear separated').text(i18n('suits', data.attributes.suits));			
	}
}


supportedTiles.deck = ['deck-big', 'deck-medium', 'deck-small'];
supportedTiles.maker = ['card-maker'];
supportedTiles.makerSign = ['makersign-medium'];
supportedTiles.taxStamp = ['tax-stamp'];
supportedTiles.pattern = ['pattern-medium'];

supportedFullViews.deck = () => new FullDeck();

elements.define("card-maker", CardMaker);
elements.define("deck-big", DeckBig);
elements.define("deck-medium", DeckMedium);
elements.define("deck-small", DeckSmall);
elements.define("deck-full", FullDeck);
elements.define("makersign-medium", MakerSign);
elements.define("tax-stamp", TaxStamp);
elements.define("pattern-medium", Pattern);
