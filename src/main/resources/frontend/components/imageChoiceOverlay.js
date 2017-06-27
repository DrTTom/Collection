// half of the imageChoice component: there is no way to layout a single 
// component both within the form grid and within an overlay
Vue.component('imagechoiceoverlay', {
    template: '<div v-if="visible" class="c-overlay"><div class="o-modal"><div class="c-card">' +
    '<header class="c-card__header c-card__item--divider"><h2 class="c-heading">{{data.name}}</h2></header>'+
        '<div class="c-card__body">' +
        '<div v-for="item in data.values" style="display: inline-block">' +
        '<label v-bind:for="item.name"><img v-bind:src="item.image"/></label><br/>' +
        '<input type="radio" name="imageChoice" v-bind:value="item.name" v-bind:id="item.name" v-model="data.selected"/>' +
        '<label v-bind:for="item.name">{{item.name}}</label></div><div class="newline"/>' + 
        '<input type="button" value="OK" v-on:click="close" class="c-button c-button--brand"/></div>' +
        '</div></div></div>',
    mounted: function() {
        CollectionEvents.imageChoiceOpened.on(this.open)
    },
    props: {
        data: {
            type: Object,
            default: {
                values: [],
                key: "empty",
                name: "empty",
                selected: "dummy"
            }
        },
        visible: {
            type: Boolean,
            default: false
        }
    },
    methods: {
        open: function(data) {
            this.data = data;
            this.visible = true;
        },
        close: function() {
            this.visible = false;
            CollectionEvents.valueChanged.send({
                key: this.data.key,
                value: this.data.selected
            });
        }
    }
})