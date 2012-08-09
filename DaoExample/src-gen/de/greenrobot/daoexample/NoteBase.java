package de.greenrobot.daoexample;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit.
/**
 * Entity mapped to table NOTE.
 */
@Test
@Test2( singleValueeee )
@Test3( key2 = "value2", key1 = 5 )
@Test4( key2 = "value2", key1 = null )
abstract public class NoteBase {

    protected Long id;
    /** Not-null value. */
    protected String text;
    @JSONIgnore
    protected String comment;
    protected java.util.Date date;

    @Deprecated
    public NoteBase() {
    }

    public NoteBase(Long id) {
        this.id = id;
    }

    @Inject( Context )
    public NoteBase(Long id, String text, String comment, java.util.Date date) {
        this.id = id;
        this.text = text;
        this.comment = comment;
        this.date = date;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /** Not-null value. */
    public String getText() {
        return text;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    @test5( key1 = value1 )
    public void setText(String text) {
        this.text = text;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public java.util.Date getDate() {
        return date;
    }

    @Test( key1 = value1 )
    public void setDate(java.util.Date date) {
        this.date = date;
    }

}
