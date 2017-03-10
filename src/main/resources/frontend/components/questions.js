Vue.component('questions', {
    template: '#questionsTemplate',
    mounted: function () {
        CardEvents.cardsLoaded.on(this.updateQuestions);
        CardEvents.questionsLoaded.on(this.updateQuestions);
    },
    props: {
        allQuestions: {
            type:Array,
            required:false,
            default:[]
        },
        questionGroups: {
            type:Array,
            required:false,
            default:[]
        },
        currentQuestion: {
            type: Object,
            required: false,
            default: {}
        },
        actorUser: {
         type: Boolean,
         required: false,
         default: false
         }
    },
    methods: {
        updateQuestions: function (response) {
            this.allQuestions= response.questions;
            this.questionGroup=[];
            this.questionGroups=[];
            var currentGroup= { name:'', elements:[] };
            for (i=0; i< this.allQuestions.length; i++) {
               if (this.allQuestions[i].form!=currentGroup.name) {
                  currentGroup.next=this.allQuestions[i].form;
                  currentGroup= { name:this.allQuestions[i].form, prev: currentGroup.name, elements:[] };
                  this.questionGroups.push(currentGroup);
                  }
               currentGroup.elements.push(this.allQuestions[i]);	
               }
            },
        answerQuestion: function (event) {
            if (actorUser==true) {
               actorUser=false;
               if (event.which == 13 || event.keyCode == 13 || event.type == 'change') {
                  CardEvents.answerQuestion.send(this.allQuestions);
                  }
               }
            // if (event.which == 0 || event.keyCode == 9)  for tab
            },
       enableUpdate: function(event) {
          actorUser=true;
          console.log("User interaction detected");
       },
       store: function(event) {
          var dummy={"x": "hallo", "y": "tt"};
          this.$http.post(url+'/submit', dummy).then( (response) => {
             console.log("should have sent");
             });
          }
    }
})
