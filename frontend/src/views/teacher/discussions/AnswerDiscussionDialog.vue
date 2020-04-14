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
          Answering Discussion
        </span>
            </v-card-title>

            <v-card-text class="text-left" v-if="editDiscussion">
                <v-container grid-list-md fluid>
                    <v-layout column wrap>
                        <v-flex xs24 sm12 md8>
                            <p><b>Student Name:</b> {{ editDiscussion.studentName }}</p>
                        </v-flex>
                        <v-flex xs24 sm12 md8>
                            <p><b>Question:</b> {{ editDiscussion.question.content }}</p>
                        </v-flex>
                        <v-flex xs24 sm12 md8>
                            <p class="d-inline-block">
                                <b>Question Options
                                </b>
                            </p>
                            <v-switch class="d-inline-block ml-2 mt-0 pt-0"
                                      :input-value="isExpanded"
                                      @change="expand()"
                            ></v-switch>
                            <v-list disabled v-if="isExpanded">
                                <v-list-item
                                        v-for="item in editDiscussion.question.options"
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
                                        <v-list-item-title v-text="item.content"></v-list-item-title>
                                    </v-list-item-content>
                                </v-list-item>
                            </v-list>
                        </v-flex>
                        <v-flex xs24 sm12 md8>
                            <p>
                                <b>Student Question:</b> {{ editDiscussion.messageFromStudent }}
                            </p>
                        </v-flex>
                        <v-flex xs24 sm12 md8>
                            <v-text-field
                                    v-model="editDiscussion.teacherAnswer"
                                    label="Your answer"
                                    data-cy="Your answer"
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
                <v-btn color="blue darken-1" @click="answerDiscussion" data-cy="sendButton"
                >Send Answer</v-btn
                >
            </v-card-actions>
        </v-card>
    </v-dialog>
</template>

<script lang="ts">
    import { Component, Model, Prop, Vue } from 'vue-property-decorator';
    import RemoteServices from '@/services/RemoteServices';
    import { Discussion } from '@/models/management/Discussion';

    @Component
    export default class AnswerDiscussionDialog extends Vue {
        @Model('dialog', Boolean) dialog!: boolean;
        @Prop({ type: Discussion, required: true }) readonly discussion!: Discussion;

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
            },
        ];

        created() {
            this.editDiscussion = new Discussion(this.discussion);
        }

        async answerDiscussion() {
            if (
                this.editDiscussion &&
                !this.editDiscussion.teacherAnswer
            ) {
                await this.$store.dispatch(
                    'error',
                    'You need to answer the question from the student.'
                );
                return;
            }

            if (this.editDiscussion) {
                try {
                    this.editDiscussion.teacherName = this.$store.getters.getUser.username;
                    const result = await RemoteServices.answerDiscussion(this.editDiscussion);
                    console.log(result);
                    this.$emit('answer-discussion', result);
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

<style lang="scss">
</style>