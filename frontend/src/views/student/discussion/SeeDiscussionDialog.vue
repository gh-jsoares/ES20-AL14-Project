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
            <v-flex xs24 sm12 md8 class="pt-0 mt-0 pb-n3 mb-n3">
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
            <v-flex xs24 sm12 md8>
              <p><b>Discussion status: </b></p>
              <p class="ml-8" v-if="seeDiscussion.visibleToOtherStudents">
                Visible to other students.
              </p>
              <p class="ml-8" v-else>
                Not visible to other students.
              </p>
              <p class="ml-8" v-if="seeDiscussion.needsAnswer">
                No teacher has answered yet.
              </p>
            </v-flex>
            <v-flex xs24 sm12 md8 class="mb-n3">
              <p><b>Messages:</b></p>
            </v-flex>
            <v-container class="mt-n8">
              <v-list-item-group
                v-for="item in seeDiscussion.messages"
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
                label="Send message"
                data-cy="Your question"
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
          data-cy="closeButton"
          >Close</v-btn
        >
        <v-btn
          color="blue darken-1"
          @click="makeNewQuestion"
          data-cy="sendButton"
          >Send Message</v-btn
        >
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script lang="ts">
import { Component, Model, Prop, Vue } from 'vue-property-decorator';
import { Discussion } from '@/models/management/Discussion';
import Message from '@/models/management/Message';
import RemoteServices from '@/services/RemoteServices';

@Component
export default class SeeDiscussionDialog extends Vue {
  @Model('dialog', Boolean) dialog!: boolean;
  @Prop({ type: Discussion, required: true }) readonly discussion!: Discussion;

  seeDiscussion!: Discussion;
  message!: Message;
  isExpanded: boolean = false;

  created() {
    this.seeDiscussion = new Discussion(this.discussion);
    this.message = new Message();
  }

  async expand() {
    this.isExpanded = !this.isExpanded;
  }

  async makeNewQuestion() {
    if (this.seeDiscussion && this.message && !this.message.message) {
      await this.$store.dispatch('error', 'You need to write something.');
      return;
    }

    if (this.seeDiscussion) {
      try {
        const result = await RemoteServices.makeNewQuestion(
          this.seeDiscussion.id,
          this.message
        );
        this.$emit('send-question', result);
      } catch (error) {
        await this.$store.dispatch('error', error);
      }
    }
  }
}
</script>

<style lang="scss"></style>
