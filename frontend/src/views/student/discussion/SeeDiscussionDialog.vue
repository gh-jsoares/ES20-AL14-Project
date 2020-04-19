<template>
  <v-dialog
    :value="dialog"
    @input="$emit('close-dialog')"
    @keydown.esc="$emit('close-dialog')"
    max-width="75%"
    max-height="80%"
  >
    <v-card>
      <v-card-title>
        <span class="headline">
          Visualizing Discussion
        </span>
      </v-card-title>

      <v-card-text class="text-start" v-if="seeDiscussion">
        <v-container grid-list-md fluid>
          <v-layout column wrap>
            <v-flex xs24 sm12 md8>
              <p><b>Question Title:</b> {{ seeDiscussion.question.title }}</p>
            </v-flex>
            <v-flex xs24 sm12 md8>
              <p><b>Question:</b> {{ seeDiscussion.question.content }}</p>
            </v-flex>
            <v-flex xs24 sm12 md8>
              <p class="d-inline-block"><b>Question Options</b></p>
              <v-switch
                class="d-inline-block ml-2 mt-0 pt-0"
                :input-value="isExpanded"
                @change="expand()"
              ></v-switch>
              <v-list disabled v-if="isExpanded">
                <v-list-item
                  v-for="item in seeDiscussion.question.options"
                  :key="item.sequence"
                >
                  <v-list-item-icon>
                    <v-icon class="mr-n3" v-if="item.correct">
                      check
                    </v-icon>
                    <v-icon class="mr-n3" v-else>
                      close
                    </v-icon>
                  </v-list-item-icon>

                  <v-list-item-content>
                    <v-list-item-title
                      v-text="item.content"
                    ></v-list-item-title>
                  </v-list-item-content>
                </v-list-item>
              </v-list>
            </v-flex>
            <v-flex xs24 sm12 md8>
              <p>
                <b>Your question:</b> {{ seeDiscussion.messageFromStudent }}
              </p>
            </v-flex>
            <v-flex xs24 sm12 md8 v-if="seeDiscussion.teacherAnswer">
              <p><b>Teacher Answer:</b> {{ seeDiscussion.teacherAnswer }}</p>
            </v-flex>
            <v-flex xs24 sm12 md8 v-else>
              <p>
                <b>No teacher has answered yet.</b>
              </p>
            </v-flex>
          </v-layout>
        </v-container>
      </v-card-text>

      <v-card-actions>
        <v-spacer />
        <v-btn
          color="blue darken-1"
          @click="$emit('close-dialog')"
          data-cy="closeButton"
          >Close</v-btn
        >
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script lang="ts">
import { Component, Model, Prop, Vue } from 'vue-property-decorator';
import { Discussion } from '@/models/management/Discussion';

@Component
export default class SeeDiscussionDialog extends Vue {
  @Model('dialog', Boolean) dialog!: boolean;
  @Prop({ type: Discussion, required: true }) readonly discussion!: Discussion;

  seeDiscussion!: Discussion;
  isExpanded: boolean = false;
  headers: Object = [
    {
      text: 'Option',
      value: 'content',
      align: 'center',
      width: '10%'
    },
    {
      text: 'Is Correct?',
      value: 'correct',
      align: 'center',
      width: '10%'
    }
  ];

  created() {
    this.seeDiscussion = new Discussion(this.discussion);
  }

  async expand() {
    this.isExpanded = !this.isExpanded;
  }
}
</script>

<style lang="scss"></style>
