from google.adk.agents import LlmAgent

decoder_ring_agent = LlmAgent(
    name="DecoderRingDecoder",
    model="gemini-2.5-pro-preview-05-06",
    description="I parse messages that are said to have been encoded with Detective Code",
    instruction="""
    You are an agent that decodes messages that have been encoded with Detective Code.

    Detective Code is defined as follows:
    {
        "A": "Z",
        "B": "Y",
        "C": "X",
        "D": "W",
        "E": "V",
        "F": "U",
        "G": "T",
        "H": "S",
        "I": "R",
        "J": "Q",
        "K": "P",
        "L": "O",
        "M": "N",
        "N": "M",
        "O": "L",
        "P": "K",
        "Q": "J",
        "R": "I",
        "S": "H",
        "T": "G",
        "U": "F",
        "V": "E",
        "W": "D",
        "X": "C",
        "Y": "B",
        "Z": "A"
    }

    Each key in the above list is said to be encoded, and its decoded value is its corresponding value.

    Here is an example encoded message:
    TVMVIZO XLUUVV WRW MLG SZEV GSV XILDYZI

    This can be decoded as:
    GENERAL COFFEE DID NOT HAVE THE CROWBAR

    Return only the decoded message.
    """
)

scrambled_message_agent = LlmAgent(
    name="ScrambledMessageDecoder",
    model="gemini-2.5-pro-preview-05-06",
    description="I parse messages that are said to have been scrambled or written with a shaky hand",
    instruction="""
    You are an agent that decodes messages that have been scrambled.

    Attempt to unscramble the message into human readable English. Note that astrology signs may appear in the message and should be decoded to said sign.

    Here is an example encoded message:
    A OCSPIRO SAW IN HET AEDD OOWSD

    This can be unscrambled as:
    A SCORPIO WAS IN THE DEAD WOODS

    Return only the unscrambled message.
    """
)

next_letter_code_agent = LlmAgent(
    name="NextLetterCodeMessageDecoder",
    model="gemini-2.5-pro-preview-05-06",
    description="I parse messages that are said to have been written using Next Letter Code",
    instruction="""
    You are an agent that decodes messages that have been encoded using Next Letter Code.

    **Instructions:**
    The encoded message can be descriphered by applying a Caesar shift right of one.
    Decode the message by advancing each character in the message to the next character in the alphabet (whitespace excluded).

    **Example:**
    Here is an example encoded message:
    SGD RGNQSDRS RTRODBS VZR MNS AX SGD ZMBHDMS QTHMR

    This can be decoded as:
    THE SHORTEST SUSPECT WAS NOT BY THE ANCIENT RUINS

    Return only the decoded message.
    """
)

root_agent = LlmAgent(
    name="MurdleSolver",
    model="gemini-2.5-pro-preview-05-06",
    description="I decode messages based on the image provided.",
    sub_agents=[
        decoder_ring_agent,
        scrambled_message_agent,
        next_letter_code_agent
    ]
)