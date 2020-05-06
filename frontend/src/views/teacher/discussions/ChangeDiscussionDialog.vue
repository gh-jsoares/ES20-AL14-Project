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
          Changing Discussion
        </span>
      </v-card-title>

      <v-card-text class="text-start" v-if="editDiscussion">
        <v-container grid-list-md fluid>
          <v-layout column wrap>
            <v-flex xs24 sm12 md8>
              <p><b>Question Title:</b> {{ editDiscussion.question.title }}</p>
            </v-flex>
            <v-flex xs24 sm12 md8>
              <p><b>Question:</b> {{ editDiscussion.question.content }}</p>
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
                  v-for="item in editDiscussion.question.options"
                  :key="item.sequence"
                  class="mb-n4"
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
                    <v-flex
                      style="opacity: 0.7"
                      xs24
                      sm12
                      md8
                      v-text="item.content"
                    ></v-flex>
                  </v-list-item-content>
                </v-list-item>
              </v-list>
            </v-flex>
            <p
              class="ml-1"
              v-if="editDiscussion.visibleToOtherStudents === true"
            >
              <b>Discussion status: </b> Visible to other students
            </p>
            <p class="ml-1" v-else>
              <b>Discussion status: </b> Not visible to other students
            </p>
            <v-flex xs24 sm12 md8 class="mb-n3">
              <p><b>Messages:</b></p>
            </v-flex>
            <v-container class="mt-n8">
              <v-list-item-group
                v-for="item in editDiscussion.messages"
                :key="item.sequence"
              >
                <div
                  style="width: 100%; display:inline-block"
                  v-if="item.userName === $store.getters.getUser.username"
                >
                  <div style="float:right; width:90%">
                    <div
                      class="ma-3 pa-2 grey lighten-4 v-text-field--rounded"
                      style="float:right"
                    >
                      <span style="overflow-wrap: break-word">
                        {{ item.message }}
                      </span>
                    </div>
                  </div>
                </div>
                <div v-else style="width: 90%">
                  <div
                    style="display: inline-block"
                    class="ma-3 pa-2 blue darken-2 white--text v-text-field--rounded"
                  >
                    <span>
                      <b>{{ item.userName }}:</b> {{ item.message }}
                    </span>
                  </div>
                </div>
              </v-list-item-group>
            </v-container>
            <v-flex xs24 sm12 md8>
              <v-text-field
                v-model="message.message"
                counter="250"
                maxlength="250"
                label="Your answer"
                data-cy="teacherAnswer"
              />
            </v-flex>
          </v-layout>
        </v-container>
      </v-card-text>

      <v-card-actions>
        <v-spacer />
        <v-btn
          color="blue darken-1"
          @click="$emit('close-dialog')"
          data-cy="cancelButton"
          >Close</v-btn
        >
        <v-btn
          color="blue darken-1"
          @click="answerDiscussion"
          data-cy="sendButton"
        >
          Send Answer</v-btn
        >
        <v-btn
          v-if="
            editDiscussion.visibleToOtherStudents === false &&
              !editDiscussion.needsAnswer
          "
          color="blue darken-1"
          @click="openDiscussion"
          data-cy="openDiscussionButton"
        >
          Open discussion to other students
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script lang="ts">
import { Component, Model, Prop, Vue } from 'vue-property-decorator';
import RemoteServices from '@/services/RemoteServices';
import { Discussion } from '@/models/management/Discussion';
import Message from '@/models/management/Message';

@Component
export default class ChangeDiscussionDialog extends Vue {
  @Model('dialog', Boolean) dialog!: boolean;
  @Prop({ type: Discussion, required: true }) readonly discussion!: Discussion;

  message!: Message;
  editDiscussion!: Discussion;
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
    this.editDiscussion = new Discussion(this.discussion);
    this.message = new Message();
  }

  async answerDiscussion() {
    if (this.editDiscussion && !this.message.message) {
      await this.$store.dispatch(
        'error',
        'You need to answer the question from the student.'
      );
      return;
    }

    if (this.editDiscussion) {
      try {
        const result = await RemoteServices.answerDiscussion(
          this.editDiscussion.id,
          this.message
        );
        this.$emit('answer-discussion', result);
      } catch (error) {
        await this.$store.dispatch('error', error);
      }
    }
  }

  async openDiscussion() {
    if (this.editDiscussion.id) {
      try {
        await RemoteServices.openDiscussion(this.editDiscussion.id);
        this.editDiscussion.visibleToOtherStudents = true;
        this.$emit('change-discussion', this.editDiscussion);
      } catch (error) {
        await this.$store.dispatch('error', error);
      }
    }
  }

  async expand() {
    this.isExpanded = !this.isExpanded;
  }
}
</script>

<style lang="scss"></style>
