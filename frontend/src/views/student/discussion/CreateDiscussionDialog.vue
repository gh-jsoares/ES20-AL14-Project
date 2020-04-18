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
          Creating Discussion
        </span>
      </v-card-title>

      <v-card-text class="text-left" v-if="newDiscussion">
        <v-container grid-list-md fluid>
          <v-layout column wrap>
            <v-flex xs24 sm12 md8>
              <p><b>Question:</b> {{ content }}</p>
            </v-flex>
            <v-flex xs24 sm12 md8>
              <p class="d-inline-block">
                <b>
                  Question Options
                </b>
              </p>
              <v-switch
                data-cy="Question Options"
                class="d-inline-block ml-2 mt-0 pt-0"
                :input-value="isExpanded"
                @change="expand()"
              ></v-switch>
              <v-list disabled v-if="isExpanded">
                <v-list-item v-for="item in options" :key="item.sequence">
                  <v-list-item-icon>
                    <v-icon class="mr-n3" v-if="item.optionId === correct">
                      check
                    </v-icon>
                    <v-icon class="mr-n3" v-else>
                      close
                    </v-icon>
                  </v-list-item-icon>
                  <v-list-item-content>
                    <v-list-item-title v-text="item.content">
                    </v-list-item-title>
                  </v-list-item-content>
                </v-list-item>
              </v-list>
            </v-flex>
            <v-flex xs24 sm12 md8>
              <v-text-field
                v-model="newDiscussion.messageFromStudent"
                label="Your question"
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
          data-cy="cancelButton"
          >Cancel</v-btn
        >
        <v-btn
          color="blue darken-1"
          @click="createDiscussion"
          data-cy="sendButton"
          >Send Discussion</v-btn
        >
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script lang="ts">
import { Component, Model, Prop, Vue } from 'vue-property-decorator';
import RemoteServices from '@/services/RemoteServices';
import { Discussion } from '@/models/management/Discussion';
import Option from '@/models/management/Option';

@Component
export default class CreateDiscussionDialog extends Vue {
  @Model('dialog', Boolean) dialog!: boolean;
  @Prop(Number) questionId!: number;
  @Prop(Number) questionAnswerId!: number;
  @Prop({ type: Array, required: true }) readonly options!: Option[];
  @Prop({ type: String, required: true }) readonly content!: String;
  @Prop(Number) correct!: number;

  newDiscussion!: Discussion;
  isExpanded: boolean = false;

  created() {
    this.newDiscussion = new Discussion();
  }

  async createDiscussion() {
    if (this.newDiscussion && !this.newDiscussion.messageFromStudent) {
      await this.$store.dispatch('error', 'You need to write a question.');
      return;
    }

    try {
      this.newDiscussion.id = this.questionAnswerId;
      const result = await RemoteServices.createDiscussion(
        this.questionId,
        this.newDiscussion
      );
      this.$emit('create-discussion', result);
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
  }

  async expand() {
    this.isExpanded = !this.isExpanded;
  }
}
</script>

<style lang="scss"></style>
