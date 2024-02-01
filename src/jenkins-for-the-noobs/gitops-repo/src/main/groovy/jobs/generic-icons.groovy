package jobs

import static java.nio.charset.StandardCharsets.UTF_8

userContent('customFolderIcons/build_and_release.svg', new ByteArrayInputStream('''\
  <svg width="800px" height="800px" viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg">
    <g>
      <path d="M878.933333 106.666667h-155.733333L682.666667 917.333333h234.666666L878.933333 106.666667z" fill="#BF360C"/>
      <path d="M704 490.666667h-85.333333v-128l-256 128v-128L106.666667 490.666667v426.666666h597.333333V490.666667z" fill="#E64A19"/>
      <path d="M192 576h85.333333v85.333333H192zM362.666667 576h85.333333v85.333333h-85.333333zM533.333333 576h85.333334v85.333333h-85.333334zM192 746.666667h85.333333v85.333333H192zM362.666667 746.666667h85.333333v85.333333h-85.333333zM533.333333 746.666667h85.333334v85.333333h-85.333334z" fill="#FFC107"/>
    </g>
  </svg>
'''.stripIndent().getBytes(UTF_8)))
